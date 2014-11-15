(ns clj-pebble.web.middleware)

(declare ^:dynamic *servlet-context-path*)

(defn wrap-servlet-context-path [handler]
  (fn [req]
    (binding [*servlet-context-path* (:context req)]
      (handler req))))