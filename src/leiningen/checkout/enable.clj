(ns leiningen.checkout.enable
  (:require [fs.core :as fs]
            [leiningen.checkout.utils :as utils]))

(defn enable
  "Enable checkouts."
  [{:keys [checkout] :as project}]
  (if (utils/checkouts-disabled?)
   (fs/rename "disabled.checkouts" "checkouts"))
  (println "# Checkouts enabled!")
  (println "# Current checkouts are:")
  (dorun
   (map (comp println (partial str "#   ")) (fs/list-dir "checkouts"))))
