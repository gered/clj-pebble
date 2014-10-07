(ns clj-pebble.standard-extensions)

(def extensions
  {:functions
    {"concat"
      (fn [& values]
        (apply str values))}

   :filters
    {}

   :tests
    {}})