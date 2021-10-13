(ns projuctivity.request.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [projuctivity.request.api :as api]
            [clojure.string :as str]))

(defn body?
 "Checks whether this is a JSON body.
  Mostly for use with validating responses with spec."
  [s]
  (try
    (json/parse-string s)
    true
    (catch com.fasterxml.jackson.core.JsonParseException ex
      false)
    (finally false)))

(defn- remove-trailing-slash
  [s]
  (str/replace s #"/$" ""))
(defn- remove-leading-slash
  [s]
  (str/replace s #"^/" ""))
(defn- add-trailing-slash
  [s]
  (if (re-find #"/$" s)
    s
    (format "%s/" s)))

(defn sanitize-hostname
  "Removes trailing slash."
  [hostname]
  (remove-trailing-slash hostname))

(defn sanitize-resource
  "Removes leading and adds trailing slash."
  [resource]
  (-> resource remove-leading-slash add-trailing-slash))

(defn inner-request
  "Makes a request using `method` to
  `hostname`/`resource` with `params`."
  [method hostname resource params]
  (try
    (let [url (format "%s/%s"
                      (sanitize-hostname hostname)
                      (sanitize-resource resource))
          resp  (method url params)]
      (json/parse-string (:body resp)))
    (catch Exception e
      (println (ex-data e))
      (throw e))))

(defrecord JSONService [hostname]
  api/HTTPRequest
  (get [service resource params]
    (inner-request http/get (:hostname service) resource params))
  (post [service resource params]
    (inner-request http/post (:hostname service) resource params)))
