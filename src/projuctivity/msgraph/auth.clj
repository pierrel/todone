(ns projuctivity.msgraph.auth
  (:require [ring.adapter.jetty :as server]
            [clojure.core.async :as async]
            [ring.util.response :as resp]
            [ring.util.codec :as codec]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as string])
  (:import (java.net InetAddress)))

(def redirect-path "/token")
(def auth-path "/auth")
(def port 3000)

(def local-hostname
  (.getHostName (InetAddress/getLocalHost)))

(def base-url (format "https://%s" local-hostname port))

(def redirect-url (format "%s%s" base-url redirect-path))
(def auth-url (format "%s%s" base-url auth-path))

(defn ms-auth-endpoint
  [tenant]
  (format "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize"
          tenant))

(defn query-string
  [clientid scopes]
  (let [params {:response_mode "query"
                :response_type "code"
                :client_id clientid
                :redirect_uri redirect-url
                :scope (string/join " "
                                    (map name scopes))}]
    (string/join "&"
                 (map (fn [[k v]]
                        (format "%s=%s"
                                (name k)
                                (codec/url-encode v)))
                      params))))

(defn ms-auth-url [clientid tenant scopes]
  (format "%s?%s"
          (ms-auth-endpoint tenant)
          (query-string clientid scopes)))

(defn handler [clientid tenant scopes f request]
  (case (:uri request)
    "/token" ; TODO: change to def above
    (if-let [token (last (re-matches #".*code=([^&]+).*$"
                                     (get request :query-string "")))]
      (f token)
      (resp/response "Could not get code. This endpoint should only be redirected-to by MS. Use /auth to get started."))

    ;; default
    (resp/redirect (ms-auth-url clientid tenant scopes))))

(defn handle-channel [c]
  (let [the-server (async/<!! c)
        token (async/<!! c)]
    (async/close! c)
    (.stop the-server)
    token))

(defn handle-token-response [channel token]
  (async/go (async/>! channel token)))

(defn get-tokens-from-code [code tenant clientid scopes]
  (let [url (format "https://login.microsoftonline.com/%s/oauth2/v2.0/token" tenant)
        params {:client_id clientid
                :scope (string/join " "
                                    (filter (partial not= "offline_access")
                                            (map string/lower-case
                                                 scopes)))
                :code code
                :redirect_uri redirect-url
                :grant_type "authorization_code"}
        resp (http/post url
                        {:accept :json
                         :content-type :application/x-www-form-urlencoded
                         :form-params params})
        body (json/parse-string (:body resp))]
    {:token (get body "access_token")
     :refresh-token (get body "refresh_token")}))

(defn get-code [clientid tenant scopes keystore-pass]
  (println (format "Running server to request credentials. Please head over to %s" auth-url))
  (let [c (async/chan)
        s (server/run-jetty (partial handler
                                     clientid
                                     tenant
                                     scopes
                                     (partial handle-token-response c))
                            {:port port
                             :join? false
                             :ssl? true
                             :keystore "keystore.jks"
                             :key-password keystore-pass})]
    (async/go (async/>! c s))
    (handle-channel c)))

(defn get-tokens [config]
  (let [{:keys [clientid tenant scopes keystorepass]} config
        all-scopes (conj scopes "offline_access")
        code (get-code clientid tenant all-scopes keystorepass)]
    (println "got code" code)
    (get-tokens-from-code code
                          tenant
                          clientid
                          all-scopes)))
