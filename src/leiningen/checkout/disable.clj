(ns leiningen.checkout.disable
  (:require [fs.core :as fs]
            [leiningen.checkout.utils :as utils]))

(defn disable
  "Disable all checkouts for a moment."
  [{:keys [checkout] :as project}]
  (if (not (utils/checkouts-disabled?))
   (fs/rename "checkouts" "disabled.checkouts"))
  (println "# Checkouts disabled!"))
