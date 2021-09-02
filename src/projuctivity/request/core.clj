(ns projuctivity.request.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [projuctivity.request.api :as api]))

(defn body? [s]
  (try
    (json/parse-string s)
    true
    (catch com.fasterxml.jackson.core.JsonParseException ex
      false)
    (finally false)))

(defn- sanitize-hostname [hostname]
  (if (re-find #"/$" hostname)
    hostname
    (format "%s/" hostname)))

(defn- sanitize-resource [resource]
  (if (re-find #"^/" resource)
    resource
    (format "/%s" resource)))

(defrecord JSONService [hostname]
  api/HTTPRequest
  (get [service resource params]
       (let [hostname (:hostname service)
             url (format "%s%s" 
                         (sanitize-hostname hostname)
                         (sanitize-resource resource))
             resp  (http/get url params)]
         (json/parse-string (:body resp)))))