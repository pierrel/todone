(ns projuctivity.msgraph.auth.urls
  "URLs related to authentication"
  (:require [ring.util.codec :as codec]
            [clojure.string :as string]
            [clojure.spec.alpha :as s]
            [cheshire.core :as json])
  (:import (java.net InetAddress)))

(s/def :auth/scopes (s/coll-of string?))

(def redirect-path "/token")
(def auth-path "/auth")
(def port 3000)

(defn is-codespaces? [hostname]
  (-> (re-matches #"^codespaces.*" hostname) nil? not))

(defn codespace-url []
  (format "%s-%s.githubpreview.dev"
          (get (System/getenv) "CODESPACE_NAME")
          (str port)))

(def on-device-hostname (.getHostName (InetAddress/getLocalHost)))

(def local-hostname
  (if (re-matches #"^codespaces.*" on-device-hostname)
    "localhost"
    on-device-hostname))

(def base-url (format "https://%s:%s" local-hostname (str port)))

(def redirect-url (format "%s%s" base-url redirect-path))
(def auth-url (format "%s%s" base-url auth-path))

(s/fdef ms-auth-endpoint
  :args (s/cat :tenant string?)
  :ret string?)
(defn ms-auth-endpoint
  [tenant]
  (format "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize"
          tenant))

(s/fdef query-string
  :args (s/cat :clientid string? :scopes (s/spec :auth/scopes))
  :ret string?)
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

(s/fdef ms-auth-url
  :args (s/cat :clientid string?
               :tenant string?
               :scopes (s/spec :auth/scopes))
  :ret string?)
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
