(ns leiningen.checkout.ln
  (:require [fs.core                  :as fs]
            [clojure.java.shell       :as shell]
            [leiningen.checkout.utils :as utils])
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
  ([]
     (checkout-candidates (fs/parent fs/*cwd*)))
  ([& checkout-roots]
     (let [checkout-roots (into #{} checkout-roots)
           checkout-roots (if-not (checkout-roots (fs/absolute-path (fs/parent fs/*cwd*)))
                            (conj checkout-roots (fs/absolute-path (fs/parent fs/*cwd*)))
                            checkout-roots)]
       (filter lein-project? (flatten (map checkout-candidates-in-dir checkout-roots))))))

(defn directory-exists? [directory]
  (and (fs/exists? directory)
       (fs/directory? directory)))

(defn link-to-checkouts [source & sources]
  (when (not (directory-exists? "checkouts"))
    (fs/mkdir "checkouts"))
  (let [sources      (conj sources source)
        target       (fs/absolute-path "checkouts/")
        sources      (filter (comp (complement directory-exists?) (partial fs/file target) fs/base-name) sources)
        source-paths (map fs/absolute-path sources)
        command      (flatten ["ln" "-s" source-paths target])]
    (apply shell/sh command)))

(defn report-no-matches [pattern search-roots candidates-for-checkout]
  (println (str "# No matching projects found for : \"" pattern "\" in search roots: " search-roots "!"))
  (println "# Candidates:")
  ;; NB: extract self-matcher to separate function?
  (dorun
   (map (comp println fs/base-name) (filter (complement (comp (partial = (fs/base-name fs/*cwd*)) fs/base-name)) candidates-for-checkout)))
  candidates-for-checkout)

(defn link-matching-candidates [matching-candidates-for-checkout]
  (println "# Linking:")
  (dorun
   (map (comp println fs/base-name) matching-candidates-for-checkout))
  (println "# into checkouts…")
  (apply link-to-checkouts matching-candidates-for-checkout)
  matching-candidates-for-checkout)

(defn report-checkouts-disabled [pattern]
  (println "# Whoops! You've tried to checkout something when you're checkouts are disabled. You should:")
  (println)
  (println "lein checkout enable; lein checkout " (str \" pattern \"))
  (println "# instead!"))

(defn ln
  "[pattern]: Link project(s) into checkouts. If PATTERN is specified, link all projects matching `.*PATTERN.*`."
  [{:keys [checkout] :as project} & [pattern]]
  (let [search-roots                            (:search-roots checkout)
        candidates-for-checkout                 (apply checkout-candidates search-roots)
        candidate-pattern                       (if pattern (re-pattern (str ".*" pattern ".*")) #".*")
        candidate-matcher                       (comp (partial re-matches candidate-pattern) fs/base-name)
        self-matcher                            (comp (partial = (fs/base-name fs/*cwd*)) fs/base-name)
        matching-candidates-for-checkout        (filter candidate-matcher candidates-for-checkout)
        matching-candidates-for-checkout        (filter (complement self-matcher) matching-candidates-for-checkout)
        sorted-matching-candidates-for-checkout (sort-by fs/base-name matching-candidates-for-checkout)]
    (cond (utils/checkouts-disabled?)
          (report-checkouts-disabled candidate-pattern)

          (= 0 (count matching-candidates-for-checkout))
          (report-no-matches candidate-pattern search-roots candidates-for-checkout)

          :link-em-in
          (link-matching-candidates sorted-matching-candidates-for-checkout))))

