(ns clj-pebble.core
  (:import (java.io StringWriter)
           (java.util Map)
           (com.mitchellbosecke.pebble PebbleEngine)
           (com.mitchellbosecke.pebble.loader ClasspathLoader FileLoader StringLoader)
           (com.mitchellbosecke.pebble.template PebbleTemplate))
  (:require [clojure.walk :refer [stringify-keys]]
            [clj-pebble.extensions :as ext]
            [clj-pebble.standard-extensions :as std]))

(defonce classpath-loader (ClasspathLoader.))
(defonce file-loader (FileLoader.))
(defonce string-loader (StringLoader.))

(defn- make-pebble-engine []
  (-> (PebbleEngine. classpath-loader)
      (ext/add-extensions-library! std/extensions)))

(defonce engine (atom (make-pebble-engine)))

(defn reset-engine! []
  (reset! engine (make-pebble-engine)))

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
    (stringify-keys context)
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
