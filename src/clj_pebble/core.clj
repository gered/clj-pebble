(ns clj-pebble.core
  (:import (java.io StringWriter)
           (java.util Map)
           (com.mitchellbosecke.pebble PebbleEngine)
           (com.mitchellbosecke.pebble.loader ClasspathLoader FileLoader StringLoader)
           (com.mitchellbosecke.pebble.template PebbleTemplate)
           (com.mitchellbosecke.pebble.extension Function Filter Test))
  (:require [clojure.walk :refer [stringify-keys]]))

(defonce classpath-loader (ClasspathLoader.))
(defonce file-loader (FileLoader.))
(defonce string-loader (StringLoader.))

(defn- make-pebble-engine []
  (PebbleEngine. classpath-loader))

(defonce engine (atom (make-pebble-engine)))

(defn reset-engine! []
  (reset! engine (make-pebble-engine)))

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

(defmacro defpebblefn [fn-name args & body]
  `(let [f#         (fn ~args ~@body)
         pebble-fn# (make-function f#)]
     (.put (.getFunctions @engine) ~fn-name pebble-fn#)))

(defmacro defpebblefilter [filter-name args & body]
  `(let [f#         (fn ~args ~@body)
         pebble-fn# (make-filter f#)]
     (.put (.getFilters @engine) ~filter-name pebble-fn#)))

(defmacro defpebbletest [test-name args & body]
  `(let [f#         (fn ~args ~@body)
         pebble-fn# (make-test f#)]
     (.put (.getTests @engine) ~test-name pebble-fn#)))

(defn- prepare-context-map [context]
  (if context
    (stringify-keys context)
    {}))

(defn- render-template [^String template context]
  (if-let [^PebbleTemplate compiled-template (.getTemplate @engine template)]
    (let [writer       (StringWriter.)
          ^Map context (prepare-context-map context)]
      (.evaluate compiled-template writer context)
      (.toString writer))))

(defn render [^String template-source & [context]]
  (.setLoader @engine string-loader)
  (render-template template-source context))

(defn render-file [^String filename & [context]]
  (.setLoader @engine file-loader)
  (render-template filename context))

(defn render-resource [^String filename & [context]]
  (.setLoader @engine classpath-loader)
  (render-template filename context))
