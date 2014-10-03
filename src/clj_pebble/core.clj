(ns clj-pebble.core
  (:import (java.io StringWriter)
           (com.mitchellbosecke.pebble PebbleEngine)
           (com.mitchellbosecke.pebble.loader DelegatingLoader ClasspathLoader FileLoader StringLoader)
           (com.mitchellbosecke.pebble.template PebbleTemplate))
  (:require [clojure.walk :refer [stringify-keys]]))

(defonce classpath-loader (ClasspathLoader.))
(defonce file-loader (FileLoader.))
(defonce string-loader (StringLoader.))

(defn- make-pebble-engine []
  (PebbleEngine. file-loader))

(defonce engine (atom (make-pebble-engine)))

(defn create-engine! []
  (reset! engine (make-pebble-engine)))

(defn prepare-context-map [context]
  (if context
    (stringify-keys context)
    {}))

(defn- render-template [^String template context]
  (if-let [^PebbleTemplate compiled-template (.getTemplate @engine template)]
    (let [writer  (StringWriter.)
          context (prepare-context-map context)]
      (.evaluate compiled-template writer context)
      (.toString writer))))

(defn render [^String template-source & [context]]
  (.setLoader @engine string-loader)
  (render-template template-source context))
