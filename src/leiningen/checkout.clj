(ns leiningen.checkout
  (:require [clojure.pprint]
            [fs.core :as fs]))

(defn pprint-str [o]
  (let [w (java.io.StringWriter.)]
    (clojure.pprint/pprint o w)
    (.toString w)))

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
  (println "Candidates:")
  (dorun (map (comp println fs/base-name) (checkout-candidates {:path "/Users/tvisher/projects" :search-depth 2}))))

(defn enable
  "Enable checkouts."
  []
  (println "Amazing enabling action!"))

(def task-dispatch
  {"ln" #'ln})

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
  (def *charnock* [project args])
  (apply (task-dispatch (first args)) (rest args)))
