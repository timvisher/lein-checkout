(ns leiningen.checkout.ln
  (:require [fs.core                  :as fs]
            [clojure.java.shell       :as shell]
            [leiningen.checkout.utils :as utils]
            [leiningen.core.project   :as project])
  (:use [leiningen.checkout.enable :only [enable]]))

(defn lein-project? [dir]
  (fs/exists? (fs/file dir "project.clj")))

(defn list-dir-absolute [dir]
  (map (partial fs/file dir) (fs/list-dir dir)))

(defn checkout-candidates-in-dir [dir]
  (let [times (or (:search-depth dir) 1)
        dir   (or (:path dir) dir)]
    (loop [times (seq (range (- times 1)))
           candidates (list-dir-absolute dir)]
      (if (not times)
        candidates
        (recur (next times) (into candidates (flatten (map list-dir-absolute candidates))))))))

(defn checkout-candidates
  "List of checkout candidates in the parent directory and other directories listed in the :checkout map in `:user` and `project.clj`"
  ([{:keys [root] :as project} & checkout-roots]
     (let [checkout-roots (into #{} checkout-roots)
           checkout-roots (if-not (checkout-roots (fs/absolute-path (fs/parent root)))
                            (conj checkout-roots (fs/absolute-path (fs/parent root)))
                            checkout-roots)]
       {:projects (into #{} (map (comp project/read fs/absolute-path #(fs/file % "project.clj")) (filter lein-project? (flatten (map checkout-candidates-in-dir checkout-roots)))))
        :search-roots checkout-roots})))

(defn directory-exists? [directory]
  (and (fs/exists? directory)
       (fs/directory? directory)))

(defn link-to-checkouts [{:keys [root] :as project} source & sources]
  (when (not (directory-exists? (fs/file root "checkouts")))
    (fs/mkdir (fs/file root "checkouts")))
  (let [sources      (conj sources source)
        target       (fs/file root "checkouts/")
        sources      (filter (comp (complement directory-exists?) (partial fs/file target) :name) sources)
        source-paths (map :root sources)
        command      (flatten ["ln" "-s" source-paths (fs/absolute-path target)])]
    (apply shell/sh command)))

(defn report-no-matches [pattern search-roots candidates-for-checkout]
  (println (str "# No matching projects found for : \"" pattern "\" in search roots: " search-roots "!"))
  (println "# Candidates:")
  ;; NB: extract self-matcher to separate function?
  (dorun
   (map println (sort (into #{} (map :name candidates-for-checkout)))))
  candidates-for-checkout)

(defn link-matching-candidates [project search-roots pattern matching-candidates-for-checkout]
  (println (str "# Linking the following projects matching \"" pattern "\" in "))
  (println (utils/bash-comment (utils/pprint-str search-roots)))
  (dorun
   (map (comp println :root) matching-candidates-for-checkout))
  (println "# into checkouts…")
  (apply link-to-checkouts project matching-candidates-for-checkout)
  matching-candidates-for-checkout)

(defn report-checkouts-disabled [pattern]
  (println "# Whoops! You've tried to checkout something when you're checkouts are disabled. You should:")
  (println)
  (println "lein checkout enable; lein checkout " (str \" pattern \"))
  (println "# instead!"))

(defn extract-vals
  "Creates a function will will return a vector of values by looking up the keys ks in map m"
  [ks]
  (fn [m] (map m ks)))

(defn ln
  "[pattern]: Link project(s) into checkouts. If PATTERN is specified, link all projects matching `.*PATTERN.*`."
  [{:keys [name dependencies] {:keys [search-roots]} :checkout :as project} & [pattern]]
  (let [dependency-coords                       (into #{} (map (comp (extract-vals [:group-id :artifact-id]) project/dependency-map) dependencies))
        {:keys [projects search-roots]}         (apply checkout-candidates project search-roots)
        candidates-for-checkout                 (filter (comp dependency-coords (extract-vals [:group :name])) projects)
        candidate-pattern                       (if pattern (re-pattern (str ".*" pattern ".*")) #".*")
        candidate-matcher                       (comp (partial re-matches candidate-pattern) :name)
        matching-candidates-for-checkout        (filter candidate-matcher candidates-for-checkout)
        sorted-matching-candidates-for-checkout (sort-by :name matching-candidates-for-checkout)]
    (cond (utils/checkouts-disabled?)
          (report-checkouts-disabled candidate-pattern)

          (= 0 (count matching-candidates-for-checkout))
          (report-no-matches candidate-pattern search-roots candidates-for-checkout)

          :link-em-in
          (link-matching-candidates project search-roots candidate-pattern sorted-matching-candidates-for-checkout))))
