(ns leiningen.checkout.utils
  (:require [clojure.pprint]
            [fs.core :as fs]))

(defn pprint-str [o]
  (let [w (java.io.StringWriter.)]
    (clojure.pprint/pprint o w)
    (.toString w)))

(defn checkouts-disabled? []
  (and (not (fs/exists? "checkouts"))
       (fs/exists? "disabled.checkouts")))
