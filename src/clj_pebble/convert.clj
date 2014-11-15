(ns clj-pebble.convert
  (:require [clj-pebble.options :refer [options]]))

(defprotocol JavaToClojure
  (to-clojure [x]))

(extend-protocol JavaToClojure
  java.util.Collection
  (to-clojure [x]
    (->> x
         (map to-clojure)
         (doall)))

  java.util.Map
  (to-clojure [x]
    (->> x
         (.entrySet)
         (reduce
           (fn [m [k v]]
             (assoc m
                    (if (and (:auto-convert-map-keywords @options)
                             (string? k))
                      (keyword k)
                      (to-clojure k))
                    (to-clojure v)))
           {})))

  java.lang.Object
  (to-clojure [x]
    x)

  nil
  (to-clojure [_]
    nil))

(defprotocol ClojureToJava
  (to-java [x]))

(extend-protocol ClojureToJava
  clojure.lang.IPersistentMap
  (to-java [x]
    (let [hashmap (new java.util.HashMap (count x))]
      (doseq [[k v] x]
        (.put hashmap
              (if (and (:auto-convert-map-keywords @options)
                       (keyword? k))
                (name k)
                (to-java k))
              (to-java v)))
      hashmap))

  clojure.lang.IPersistentCollection
  (to-java [x]
    (let [array (new java.util.ArrayList (count x))]
      (doseq [item x]
        (.add array (to-java item)))
      array))

  java.lang.Object
  (to-java [x]
    x)

  nil
  (to-java [_]
    nil))

(defn java->clojure [x]
  (to-clojure x))

(defn clojure->java [x]
  (to-java x))