(ns msgraph.auth
  (:require [ring.adapter.jetty :as server]
            [clojure.core.async :as async]
            [ring.util.response :as resp]
            [ring.util.codec :as codec]
            [clojure.string :as string])
  (:import (java.net InetAddress)))

(def redirect-path "/token")
(def auth-path "/auth")
(def port 3000)

(def local-hostname
  (.getHostName (InetAddress/getLocalHost)))

(defn redirect-url
  []
  (let [hostname (.getHostName (InetAddress/getLocalHost))]
    (format "https://%s%s" hostname redirect-path)))

(defn auth-endpoint
  [tenant]
  (format "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize"
          tenant))

(defn query-string
  [clientid scopes host]
  (let [params {:response_mode "query"
                :response_type "code"
                :client_id clientid
                :redirect_uri (redirect-url)
                :scope (string/join " "
                                    (map name scopes))}]
    (string/join "&"
                 (map (fn [[k v]]
                        (format "%s=%s"
                                (name k)
                                (codec/url-encode v)))
                      params))))

(defn auth-url [clientid tenant scopes host]
  (format "%s?%s"
          (auth-endpoint tenant)
          (query-string clientid scopes host)))

(defn handler [clientid tenant scopes host f request]
  (case (:uri request)
    "/test" ; TODO: remove
    (resp/response "Testing endpoint")

    "/auth" ; TODO: change to def above
    (resp/redirect (auth-url clientid tenant scopes host))

    "/token" ; TODO: change to def above
    (do
      (if-let [token (last (re-matches #".*code=([^&]+).*$"
                                       (:query-string request)))]
        (f token)
        (println "Could not get token"))
      (resp/response (format "Got it")))

    ;; default
    (resp/response "Hit nothing")))

(defn handle-channel [c]
  (let [the-server (async/<! c)
        token (async/<! c)]
    (async/close! c)
    token))

(defn handle-token-response [channel token]
  (println (format "Got token %s" token))
  (async/>!! channel token))

(defn get-token [clientid tenant scopes keystore-pass]
  (let [c (async/chan)
        s (server/run-jetty (partial handler
                                     clientid
                                     tenant
                                     scopes
                                     (format "%s:%d" local-hostname port)
                                     (partial handle-token-response c))
                            {:port port
                             :join? false
                             :ssl? true
                             :keystore "keystore.jks"
                             :key-password keystore-pass})]
    (async/>!! c s)
    (handle-channel c)))
