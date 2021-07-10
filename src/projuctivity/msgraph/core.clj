(ns projuctivity.msgraph.core
  (:use [projuctivity.msgraph.pure])
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.spec.alpha :as s]
            [java-time :as t]
            [projuctivity.msgraph.utils :as utils]
            [projuctivity.msgraph.auth :as auth]))

(def base-url "https://graph.microsoft.com")

(s/def :msgraph/body (s/with-gen
                       (s/and string?
                              #(try
                                 (json/parse-string %)
                                 true
                                 (catch com.fasterxml.jackson.core.JsonParseException ex
                                   false)
                                 (finally false)))
                       #(s/gen #{"{}"
                                 "{'hello': 'there'}"})))
(s/def :msgraph/response (s/keys :req-un [:msgraph/body]))

(s/fdef httpget
  :args (s/cat :url string?
               :params map?)
  :ret (s/spec :msgraph/response))
(def httpget http/get)

(s/fdef get-resource
  :args (s/alt :binary (s/cat :config :projuctivity.msgraph.api/config
                              :resource string?)
               :trinary (s/cat :config :projuctivity.msgraph.api/config
                               :resource string?
                               :params map?)
               :quaternary (s/cat :config :projuctivity.msgraph.api/config
                                  :resource string?
                                  :params map?
                                  :token string?))
  :ret map?)
(defn get-resource
  "Sends a GET request to the desired resource with parameters and token."
  ([config resource params token]
   (try
     (let [url (format "%s/v1.0/%s" base-url resource)
           params {:headers {:authorization (format "Bearer %s" token)}
                   :query-params params}
           resp (httpget url params)]
       (json/parse-string (:body resp)))
     (catch Exception e
       (if (= (:status (ex-data e)) 401)
         (do
           (auth/refresh-token config)
           (get-resource resource params))
         (throw e)))))
  ([config resource params]
   (let [token (auth/token config)]
     (get-resource config resource params token)))
  ([config resource]
   (get-resource config resource {})))

(defn- events-between-raw
  [config d1 d2]
  (let [resp (get-resource config
                           "me/calendarview"
                           (zipmap ["startdatetime"
                                    "enddatetime"]
                                   (map str
                                        (sort t/before? [d1 d2]))))]
    (get resp "value")))

(defn events-between
  "Returns events between d1 and d2. Does not handle paging."
  [config d1 d2]
  (map to-event (events-between-raw config d1 d2)))

