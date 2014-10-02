(ns clj-pebble.core
  (:import (java.io StringWriter)
           (com.mitchellbosecke.pebble PebbleEngine)
           (com.mitchellbosecke.pebble.loader DelegatingLoader ClasspathLoader FileLoader StringLoader)
           (com.mitchellbosecke.pebble.template PebbleTemplate))
  (:require [clojure.walk :refer [stringify-keys]]))

(defonce file-loader
  (DelegatingLoader.
    [(ClasspathLoader.)
     (FileLoader.)]))

(defonce string-loader
  (StringLoader.))

(defn- make-pebble-engine []
  (PebbleEngine. file-loader))

(defonce engine (atom (make-pebble-engine)))

(defn create-engine! []
  (reset! engine (make-pebble-engine)))

(defn prepare-context-map [context]
  (if context
    (stringify-keys context)
    {}))

(defn render [^String template-source & [context]]
  (.setLoader @engine string-loader)
  (if-let [^PebbleTemplate compiled-template (.getTemplate @engine template-source)]
    (let [writer  (StringWriter.)
          context (prepare-context-map context)]
      (.evaluate compiled-template writer context)
      (.toString writer))))
