(ns clj-pebble.options)

(defonce options
  (atom
    {:cache                     true
     :auto-convert-map-keywords true}))