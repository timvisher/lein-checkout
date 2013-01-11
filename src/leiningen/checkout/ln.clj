(ns leiningen.checkout.ln
  (:require [fs.core :as fs]))

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

(defn ln
  "[pattern]: Link project(s) into checkouts. If PATTERN is specified, link all projects matching `.*PATTERN.*`."
  [& [pattern]]
  
  (println "Candidates:")
  (let [candidates-for-checkout            (checkout-candidates {:path "/Users/tvisher/projects" :search-depth 2})
        candidates-for-checkout-base-names (map fs/base-name candidates-for-checkout)
        candidate-pattern                  (if pattern (re-pattern (str ".*" pattern ".*")) #".*")
        candidate-matcher                  (partial re-matches candidate-pattern)
        print-base-name                    (comp println fs/base-name)
        candidates                         (sort (filter candidate-matcher candidates-for-checkout-base-names))]
   (dorun
    (map print-base-name candidates))
   candidates))

(comment
  (ln "clie")
  )
