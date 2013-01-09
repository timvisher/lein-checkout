(ns leiningen.checkout
  (:require [clojure.pprint]
            [fs.core :as fs]))

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

(comment
  (def *charnock* (atom []))
  )

(defn lein-project? [dir]
  (swap! *charnock* conj (fs/with-cwd  (fs/file dir "project.clj")))
  (fs/exists? (fs/file dir "project.clj")))

(defn checkout-candidates-in-dir [dir]
  (if (:path dir))
  (map fs/parent (filter fs/exists? (map (comp #(fs/file % "project.clj") (partial fs/file dir)) (fs/list-dir dir)))))

(comment
  (checkout-candidates-in-dir {:path "/Users/tvisher/projects" :search-depth 2})
  (fs/find-files "/Users/tvisher/projects" #"project.clj")

  (defn list-dir-absolute [dir]
    (map (partial fs/file dir) (flatten (map (comp fs/list-dir) [dir]))))

  (defn list-dir-absolute [dir]
    (map (partial fs/file dir) (fs/list-dir dir)))

  (loop [times (seq (range 0 (- 3 1)))
         candidates (list-dir-absolute "/Users/tvisher/projects")]
    (if (not times)
      candidates
      (recur (next times) (into candidates (flatten (map list-dir-absolute candidates))))))

  (flatten (map list-dir-absolute (flatten (map list-dir-absolute (list-dir-absolute "/Users/tvisher/projects")))))

;;; for search depth
;;; list-dir
;;; filter for dirs
;;; list-dirs in them
;;; filter for dirs
  )


(defn checkout-candidates
  "List of checkout candidates in the parent directory and other directories listed in the :checkout map in `:user` and `project.clj`"
  ([]
     (checkout-candidates (fs/parent fs/*cwd*)))
  ([& checkout-roots]
     (let [checkout-roots (into #{} checkout-roots)
           checkout-roots (if-not (checkout-roots (fs/parent fs/*cwd*))
                            (conj checkout-roots (fs/parent fs/*cwd*)))]
       (def *charnock* checkout-roots)
       (reduce into [] (map checkout-candidates-in-dir checkout-roots)))))

(comment
  (checkout-candidates {:path "/Users/tvisher/projects" :search-depth 2})
  (map checkout-candidates-in-dir *charnock*)

  keep
  ((into #{} [(fs/parent fs/*cwd*)]) (fs/parent fs/*cwd*))
  )

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