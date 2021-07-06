(ns projuctivity.msgraph.core
  (:use [projuctivity.msgraph.pure])
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.spec.alpha :as s]
            [java-time :as t]
            [projuctivity.msgraph.utils :as utils]
            [projuctivity.msgraph.auth :as auth]))

(def scopes ["user.read"
             "Tasks.ReadWrite"
             "Calendars.ReadWrite"])

(def config (s/conform :auth/config
                       (assoc (utils/load-edn ".config.edn")
                              :auth/scopes scopes)))

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
  :args (s/cat :resource string?
               :params map?
               :token string?)
  :ret map?)
(defn get-resource
  "Sends a GET request to the desired resource with parameters and token."
  ([resource params token]
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
  ([resource params]
   (let [token (auth/token config)]
     (get-resource resource params token)))
  ([resource]
   (get-resource resource {})))

(defn events-raw
  "Returns events between now and `d` days."
  [d]
  (let [now (t/instant)
        other (t/plus now (t/days d))
        resp (get-resource "me/calendarview"
                           (zipmap ["startdatetime"
                                    "enddatetime"]
                                   (sort t/before? [now other])))]
    (get resp "value")))

(defn past-events
  "Gets events between now and 90 days ago."
  []
  (map to-event (events-raw -90)))

(defn future-events
  "Gets events between now and 90 days from now."
  []
  (map to-event (events-raw 90)))
