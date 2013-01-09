(ns leiningen.checkout
  (:require [clojure.pprint]))

(defn pprint-str [o]
  (let [w (java.io.StringWriter.)]
    (clojure.pprint/pprint o w)
    (.toString w)))

(defn rm
  "[pattern]: Remove all checkouts. If PATTERN is specified, only checkouts matching that pattern will be removed"
  []
  (println "Amazing rm action!"))

(defn disable
  "Disable all checkouts for a moment."
  []
  (println "Amazing disable action!"))

(defn ln
  "[pattern]: Link project(s) into checkouts. If PATTERN is specified, link all projects matching PATTERN."
  []
  (println "Amazing symlinking action!"))

(defn enable
  "Enable checkouts."
  []
  (println "Amazing enabling action!"))

(defn
  ^{:subtasks [#'leiningen.checkout/ln
               #'leiningen.checkout/rm
               #'leiningen.checkout/enable
               #'leiningen.checkout/disable]}
  checkout
  "Manage your checkouts directory.

Add a `:checkout` key to your project or `:user` profile which is a vector of directory paths under which projects to be considered for checkouts can be found.

ln: add a project to your checkouts.
rm: remove a project from your checkouts.
disable: temporarily move aside checkouts.
enable: re-enable checkouts, moving it back into place.

Call `lein help checkout` for more options."
  [project & args]
  (println (pprint-str [project args])))

(comment
  (let [w (java.io.StringWriter.)] (clojure.pprint/pprint {} w) (.toString w))
  (ns-resolve *ns* 'conj)
  )