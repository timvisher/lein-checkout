(ns leiningen.checkout.utils
  (:require [clojure.pprint]
            [fs.core :as fs])
  (:import [java.io BufferedReader StringReader]))

(defn pprint-str [o]
  (with-open [w (java.io.StringWriter.)]
    (clojure.pprint/pprint o w)
    (.toString w)))

(defn bash-comment [o]
  {:pre [(= java.lang.String (type o))]}
  (with-open [r (BufferedReader. (StringReader. o))]
    (->> (line-seq r)
         (map (partial str "# "))
         (clojure.string/join (System/getProperty "line.separator")))))

(defn checkouts-disabled? []
  (and (not (fs/exists? "checkouts"))
       (fs/exists? "disabled.checkouts")))
