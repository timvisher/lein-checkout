(ns leiningen.checkout.rm
  (:require [fs.core                  :as fs]
            [leiningen.checkout.utils :as utils]))

(defn report-no-matches [non-matched-pattern candidates]
  (println (str "No matching projects found for: \""
                non-matched-pattern
                "\" in checkouts!"))
  (println "Candidates:")
  (dorun
   (map println candidates))
  candidates)

(defn rm-checkouts [pattern checkouts-to-rm]
  (println (str "# `rm`ing the following projects matching \"" pattern "\":"))
  (dorun
   (map println checkouts-to-rm))
  (dorun
   (map (comp fs/delete-dir (partial fs/file "checkouts")) checkouts-to-rm))
  checkouts-to-rm)

(defn rm
  "[pattern]: Remove all checkouts. If PATTERN is specified, only checkouts matching that pattern will be removed"
  [{:keys [checkout] :as project} & [pattern]]
  (let [current-checkouts (fs/list-dir "checkouts")
        candidate-pattern (if pattern (re-pattern (str ".*" pattern ".*")) #".*")
        candidate-matcher (partial re-matches candidate-pattern)
        matching-checkouts (filter candidate-matcher current-checkouts)]
    (if (= 0 (count matching-checkouts))
      (report-no-matches candidate-pattern current-checkouts)
      (rm-checkouts candidate-pattern matching-checkouts))))
