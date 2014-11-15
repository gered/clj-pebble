(defproject clj-pebble "0.2.0"
  :description "Clojure wrapper for the Pebble Java templating engine."
  :url "https://github.com/gered/clj-pebble"
  :license {:name "BSD 3-Clause License"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.mitchellbosecke/pebble "1.1.0"]
                 [cheshire "5.3.1"]
                 [org.apache.commons/commons-lang3 "3.3.2"]]
  :profiles {:dev {:dependencies [[pjstadig/humane-test-output "0.6.0"]]
                   :injections   [(require 'pjstadig.humane-test-output)
                                  (pjstadig.humane-test-output/activate!)]}})
