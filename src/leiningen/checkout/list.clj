(ns leiningen.checkout.list
  (:refer-clojure :exclude [list])
  (:require [fs.core                  :as fs]
            [leiningen.checkout.utils :as utils]))

(defn report-checkouts-disabled []
  (println "# Whoops! You tried to list your checkouts when they are disabled. You should: ")
  (println)
  (println "  lein checkout enable; lein checkout list"))

(defn list-current-checkouts [{:keys [root] :as project}]
  (let [checkouts (fs/list-dir (fs/file root "checkouts"))]
    (println "# Current checkouts are:")
    (dorun
     (map println checkouts))))

(defn list [project]
  (if (utils/checkouts-disabled?)
    (report-checkouts-disabled)
    (list-current-checkouts project)))
