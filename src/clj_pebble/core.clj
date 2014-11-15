(ns clj-pebble.core
  (:import (java.io StringWriter)
           (java.util Map)
           (com.mitchellbosecke.pebble PebbleEngine)
           (com.mitchellbosecke.pebble.loader ClasspathLoader FileLoader StringLoader)
           (com.mitchellbosecke.pebble.template PebbleTemplate)
           (com.google.common.cache Cache CacheBuilder))
  (:require [clojure.walk :refer [stringify-keys]]
            [clj-pebble.extensions :as ext]
            [clj-pebble.standard-extensions :as std]
            [clj-pebble.options :refer [options]]))

(defonce classpath-loader (ClasspathLoader.))
(defonce file-loader (FileLoader.))
(defonce string-loader (StringLoader.))

(defn- set-cache! [^PebbleEngine engine size]
  (.setTemplateCache
    engine
    (-> (CacheBuilder/newBuilder)
        (.maximumSize size)
        (.build))))

(defn- apply-options! [^PebbleEngine engine]
  (doseq [[k v] @options]
    (cond
      (= :cache k)
      (set-cache! engine (if v 200 0)))))

(defn- make-pebble-engine []
  (let [engine (-> (PebbleEngine. classpath-loader)
                   (ext/add-extensions-library! std/extensions))]
    (apply-options! engine)
    engine))

(defonce engine (atom (make-pebble-engine)))

(defn reset-engine! []
  (reset! engine (make-pebble-engine)))

(defn set-options! [& opts]
  (doseq [[k v] (apply hash-map opts)]
    (swap! options assoc k v))
  (apply-options! @engine))

(defmacro defpebblefn [fn-name args & body]
  `(let [f# (fn ~args ~@body)]
     (ext/add-function! @engine ~fn-name f#)))

(defmacro defpebblefilter [filter-name args & body]
  `(let [f# (fn ~args ~@body)]
     (ext/add-filter! @engine ~filter-name f#)))

(defmacro defpebbletest [test-name args & body]
  `(let [f# (fn ~args ~@body)]
     (ext/add-test! @engine ~test-name f#)))

(defn- prepare-context-map [context]
  (if context
    (if (:auto-convert-map-keywords @options)
      (stringify-keys context)
      context)
    {}))

(defn- render-template [^String template context]
  (if-let [^PebbleTemplate compiled-template (.getTemplate ^PebbleEngine @engine template)]
    (let [writer       (StringWriter.)
          ^Map context (prepare-context-map context)]
      (.evaluate compiled-template writer context)
      (.toString writer))))

(defn render [^String template-source & [context]]
  (.setLoader ^PebbleEngine @engine string-loader)
  (render-template template-source context))

(defn render-file [^String filename & [context]]
  (.setLoader ^PebbleEngine @engine file-loader)
  (render-template filename context))

(defn render-resource [^String filename & [context]]
  (.setLoader ^PebbleEngine @engine classpath-loader)
  (render-template filename context))
