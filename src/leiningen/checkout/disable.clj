(ns leiningen.checkout.disable
  (:require [fs.core :as fs]
            [leiningen.checkout.utils :as utils]))

(defn disable
  "Disable all checkouts for a moment."
  [{:keys [checkout] :as project}]
  ;; NB: This should check that checkouts exists _and_ that disabled.checkouts does not exist and report error if not
  (if (not (utils/checkouts-disabled?))
   (fs/rename "checkouts" "disabled.checkouts"))
  (println "# Checkouts disabled!"))
