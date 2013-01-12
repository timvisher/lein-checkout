(ns leiningen.checkout.ln
  (:require [fs.core            :as fs]
            [clojure.java.shell :as shell])
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
       (filter lein-project? (reduce into [] (map checkout-candidates-in-dir checkout-roots))))))

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

(defn ln
  "[pattern]: Link project(s) into checkouts. If PATTERN is specified, link all projects matching `.*PATTERN.*`."
  [& [pattern]]
  (let [candidates-for-checkout                   (checkout-candidates {:path "/Users/tvisher/projects" :search-depth 2})
        candidate-pattern                         (if pattern (re-pattern (str ".*" pattern ".*")) ".*")
        candidate-matcher                         (comp (partial re-matches candidate-pattern) fs/base-name)
        candidates-for-checkout                   (filter candidate-matcher candidates-for-checkout)
        sorted-candidates-for-checkout            (sort-by fs/base-name candidates-for-checkout)
        sorted-candidates-for-checkout-base-names (map fs/base-name sorted-candidates-for-checkout)]
    (println "Linking:")
    (dorun
     (map println sorted-candidates-for-checkout-base-names))
    (println "into checkouts…")
    (apply link-to-checkouts sorted-candidates-for-checkout)
    candidates-for-checkout))

(comment
  (ln "clie")
  )
