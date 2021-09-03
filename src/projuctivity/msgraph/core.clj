(ns projuctivity.msgraph.core
  (:require [projuctivity.request.api :as req-api]
            [projuctivity.request.core :as request]
            [clojure.spec.alpha :as s]
            [java-time :as t]
            [projuctivity.msgraph.auth :as auth]
            [projuctivity.msgraph.pure :as pure])
  (:import [projuctivity.request.core JSONService]))

(def base-url "https://graph.microsoft.com")
(def service (JSONService. base-url))

(defn contains-base? [url]
  (re-matches #"^https.*" url))

(s/def :msgraph/body (s/with-gen
                       (s/and string?
                              request/body?)
                       #(s/gen #{"{}"
                                 "{'hello': 'there'}"})))
(s/def :msgraph/response (s/keys :req-un [:msgraph/body]))

(defn auth [config]
  (auth/refresh-token config))

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
     (let [params {:headers {:authorization (format "Bearer %s" token)}
                   :query-params params}]
       (req-api/get service resource params))
     (catch Exception e
       (if (= (:status (ex-data e)) 401)
         (do
           (auth/refresh-token config)
           (get-resource config resource params))
         (throw e)))))
  ([config resource params]
   (let [token (-> config auth/tokens :token)]
     (get-resource config resource params token)))
  ([config resource]
   (get-resource config resource {})))

(defn- events-between-raw
  "Pages through events and returns a lazy sequence of raw event json."
  ([config d1 d2]
   (let [args (zipmap ["startdatetime"
                       "enddatetime"]
                      (map str
                           (sort t/before? [d1 d2])))
         resp (get-resource config
                            "v1.0/me/calendarview"
                            args)
         values (get resp "value")
         thenext (get resp "@odata.nextLink")]
     (lazy-seq (concat values
                       (events-between-raw config thenext)))))
  ([config nextlink]
   (if nextlink
     (let [resp (get-resource config
                              nextlink)
           values (get resp "value")
           thenext (get resp "@odata.nextLink")]
       (lazy-seq (concat values
                         (events-between-raw config thenext)))))))

(defn events-between
  "Lazy seq of `Event`s between d1 and d2."
  [config d1 d2]
  (map pure/to-event (events-between-raw config d1 d2)))

