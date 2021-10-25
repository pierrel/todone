(ns projuctivity.request.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [projuctivity.request.api :as api]
            [clojure.string :as str]))

(def injected (atom nil))
(defn inject-handler! [fn]
  "Uses fn instead of the default implementation."
  (reset! injected fn))

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
  (if @injected
    (@injected method hostname resource params)
    (try
      (let [method-fn (case method
                        :get http/get
                        :post http/post
                        http/get)
            url (format "%s/%s"
                        (sanitize-hostname hostname)
                        (sanitize-resource resource))
            resp  (method-fn url params)]
        (json/parse-string (:body resp)))
      (catch Exception e
        (println (ex-data e))
        (throw e)))))

(defrecord JSONService [hostname]
  api/HTTPRequest
  (get [service resource params]
    (inner-request :get (:hostname service) resource params))
  (post [service resource params]
    (inner-request :post (:hostname service) resource params)))
