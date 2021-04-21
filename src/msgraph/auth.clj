(ns msgraph.auth
  (:require [ring.adapter.jetty :as server]
            [ring.util.response :as resp]
            [ring.util.codec :as codec]
            [clojure.string :as string])
  (:import (java.net InetAddress)))

(def redirect-path "/token")
(def auth-path "/auth")

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
    "/test"
    (resp/response "Testing endpoint")

    "/auth"
    (resp/redirect (auth-url clientid tenant scopes host))

    "/token"
    (do
      (if-let [token (last (re-matches #".*code=([^&]+).*$"
                                       (:query-string request)))]
        (f token)
        (println "Could not get token"))
      (resp/response (format "Got it")))

    ;; default
    (resp/response "Hit nothing")))

(defn get-token [clientid tenant scopes keystore-pass f]
  (server/run-jetty (partial handler
                             clientid
                             tenant
                             scopes
                             (format "%s:3000" local-hostname)
                             f)
                    {:port 3000
                     :join? false
                     :ssl? true
                     :keystore "keystore.jks"
                     :key-password keystore-pass}))
