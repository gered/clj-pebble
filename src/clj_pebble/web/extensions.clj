(ns clj-pebble.web.extensions
  (:import (java.net URI))
  (:require [clojure.string :as str]
            [clj-pebble.web.middleware :refer [*servlet-context-path*]]
            [clj-pebble.options :refer [options]])
  (:use [clj-pebble.utils]))

;; TODO: while 'public' is the default with Compojure, applications can override with something else ...
;;       should make this customizable (some option added to clj-pebble.options likely ...)
(def root-resource-path "public")

(defn- get-context-url [url]
  (str *servlet-context-path* url))

(defn- relative-url? [url]
  (if-not (str/blank? url)
    (let [uri (new URI url)]
      (str/blank? (.getScheme uri)))))

(defn- get-resource-modification-timestamp [^String resource-url]
  (if (relative-url? resource-url)
    (->> (str root-resource-path resource-url)
         (get-context-url)
         (get-resource-modification-date))))

(defn- get-url-string [url]
  (if-let [modification-timestamp (get-resource-modification-timestamp url)]
    ; because it looks kind of dumb to have '?0' at the end of URLs when running from a jar ...
    (if (= modification-timestamp 0)
      url
      (str url "?" modification-timestamp))
    url))

(defn- minified-url? [url]
  (re-matches #"^(.+\.)min\.(css|js)$" url))

(defn- make-minified-url [^String url]
  (let [pos (.lastIndexOf url (int \.))]
    (if (> pos -1)
      (let [name      (subs url 0 pos)
            extension (subs url (inc pos))]
        (str name ".min." extension))
      url)))

(defn- get-minified-resource-url [url]
  (if (or (not (:check-for-minified-web-resources @options))
          (minified-url? url))
    url
    (let [minified-url (make-minified-url url)]
      (if (get-resource-path (str root-resource-path minified-url))
        minified-url
        url))))

; defined using the same type of map structure as in clj-pebble.standard-extensions

(defonce extensions
  {:functions
   {"path"
    {:fn (fn [url]
           (get-context-url url))}

    "stylesheet"
    {:fn (fn [url & [media]]
           (let [fmt           (if media
                                 "<link href=\"%s\" rel=\"stylesheet\" type=\"text/css\" media=\"%s\" />"
                                 "<link href=\"%s\" rel=\"stylesheet\" type=\"text/css\" />")
                 resource-path (get-minified-resource-url url)]
             (format fmt (get-url-string resource-path) media)))}

    "javascript"
    {:fn (fn [url]
           (let [fmt           "<script type=\"text/javascript\" src=\"%s\"></script>"
                 resource-path (get-minified-resource-url url)]
             (format fmt (get-url-string resource-path))))}}

   :filters
   {}

   :tests
   {}})