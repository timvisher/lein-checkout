(ns leiningen.checkout
  (:require [fs.core                  :as fs]
            [leiningen.checkout.utils :as utils])
  (:use [leiningen.checkout.ln      :only [ln]]
        [leiningen.checkout.rm      :only [rm]]
        [leiningen.checkout.enable  :only [enable]]
        [leiningen.checkout.disable :only [disable]]))

(def task-dispatch
  {"ln" #'ln
   :default #'ln})

(defn
  ^{:subtasks [#'ln
               #'rm
               #'enable
               #'disable]}
  checkout
  "Manage your checkouts directory.

Add a `:checkout` key to your project or `:user` profile which is a vector of directory paths under which projects to be considered for checkouts can be found.

ln: add a project to your checkouts.
rm: remove a project from your checkouts.
disable: temporarily move aside checkouts.
enable: re-enable checkouts, moving it back into place.

Call `lein help checkout` for more options."
  [project & args]
  #_(println (utils/pprint-str args))
  (apply
   (or (task-dispatch (first args)) (:default task-dispatch))
   project
   (if (task-dispatch (first args))
     (rest args)
     args)))
