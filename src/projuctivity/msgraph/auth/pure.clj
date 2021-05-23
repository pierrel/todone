(ns projuctivity.msgraph.auth.pure
  "Pure functions to handle authentication."
  (:require [ring.util.codec :as codec]
            [clojure.string :as string]
            [cheshire.core :as json])
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


(defn token-request-params [code tenant clientid scopes refresh?]
  (let [url (format "https://login.microsoftonline.com/%s/oauth2/v2.0/token"
                    tenant)
        scope (string/join " "
                           (filter (partial not= "offline_access")
                                   (map string/lower-case
                                        scopes)))
        base-params {:client_id clientid
                     :scope scope
                     :redirect_uri redirect-url}]
    {:url url
     :params (merge base-params
                    (if refresh?
                      {:grant_type "refresh_token"
                       :refresh_token code}
                      {:grant_type "authorization_code"
                       :code code}))}))

(defn tokens-from-token-response [resp]
  (let [body (json/parse-string (:body resp))]
    {:token (get body "access_token")
     :refresh-token (get body "refresh_token")}))
