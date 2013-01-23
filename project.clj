(defproject org.clojars.timvisher/lein-checkout "0.4.2"
  :description "Manage your checkouts directory with ease"
  :url "https://github.com/timvisher/lein-checkout"
  :repositories [["clojars" {:url "https://clojars.org/repo"
                             :creds :gpg}]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[fs "1.3.3"]]
  :eval-in-leiningen true)