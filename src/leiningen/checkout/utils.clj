(ns leiningen.checkout.utils
  (:require [clojure.pprint]))

(defn pprint-str [o]
  (let [w (java.io.StringWriter.)]
    (clojure.pprint/pprint o w)
    (.toString w)))
