(ns clj-pebble.extensions
  (:import (java.util Map)
           (com.mitchellbosecke.pebble PebbleEngine)
           (com.mitchellbosecke.pebble.extension Function Filter Test)))

(defn- get-sorted-args [args-map]
  (->> args-map
       (map
         (fn [[k v]]
           [(Integer/parseInt k) v]))
       (sort-by first)
       (map second)))

(defn make-function [f]
  (reify Function
    (getArgumentNames [_] nil)
    (execute [_ args]
      (apply f (get-sorted-args args)))))

(defn make-filter [f]
  (reify Filter
    (getArgumentNames [_] nil)
    (apply [_ input args]
      (apply f (concat [input] (get-sorted-args args))))))

(defn make-test [f]
  (reify Test
    (getArgumentNames [_] nil)
    (apply [_ input args]
      (boolean (apply f (concat [input] (get-sorted-args args)))))))

(defn add-function! [^PebbleEngine engine ^String name f]
  (.put (.getFunctions engine) name (make-function f)))

(defn add-filter! [^PebbleEngine engine ^String name f]
  (.put (.getFilters engine) name (make-filter f)))

(defn add-test! [^PebbleEngine engine ^String name f]
  (.put (.getTests engine) name (make-test f)))

(defn add-extensions-library! [^PebbleEngine engine {:keys [functions filters tests]}]
  (doseq [[name func] functions]
    (add-function! engine name func))
  (doseq [[name func] filters]
    (add-filter! engine name func))
  (doseq [[name func] tests]
    (add-test! engine name func))
  engine)