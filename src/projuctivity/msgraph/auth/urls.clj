(ns projuctivity.msgraph.auth.urls
  "URLs related to authentication"
  (:require [ring.util.codec :as codec]
            [clojure.string :as string]
            [clojure.spec.alpha :as s])
  (:import [java.net InetAddress]))

(s/def :auth/scopes (s/coll-of string?))
(s/def :projuctivity.msgraph.auth.urls/auth-url-args
  (s/cat :clientid :auth/clientid
         :redirect-uri :auth/redirect-uri
         :tenant :auth/tenant
         :scopes (s/spec :auth/scopes)))

(defn base-uri [redirect-uri]
   (let [uri (java.net.URI. redirect-uri)]))
(defn redirect-path [redirect-uri])

(s/fdef ms-auth-endpoint
  :args (s/cat :tenant string?)
  :ret string?)
(defn ms-auth-endpoint
  [tenant]
  (format "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize"
          tenant))

(s/fdef query-string
  :args (s/cat :clientid string?
               :redirect-uri (s/or :defined string?
                               :undefined nil?)
               :scopes (s/spec :auth/scopes))
  :ret string?)
(defn query-string
  [clientid redirect-uri scopes]
  (let [params {:response_mode "query"
                :response_type "code"
                :client_id clientid
                :redirect_uri (redirect-url redirect-uri)
                :scope (string/join " "
                                    (map name scopes))}]
    (string/join "&"
                 (map (fn [[k v]]
                        (format "%s=%s"
                                (name k)
                                (codec/url-encode v)))
                      params))))

(s/fdef ms-auth-url
  :args :projuctivity.msgraph.auth.urls/auth-url-args
  :ret string?)
(defn ms-auth-url [clientid redirect-uri tenant scopes]
  (format "%s?%s"
          (ms-auth-endpoint tenant)
          (query-string clientid redirect-uri scopes)))

(s/fdef test-auth-url
  :args :projuctivity.msgraph.auth.urls/auth-url-args
  :ret string?)
(defn test-auth-url [clientid _ _ scopes]
  (format "http://%s:3001?%s"
          (public-hostname)
          (query-string clientid scopes)))

(defn token-request-params [code tenant clientid client-secret scopes refresh? server-config]
  (let [url (format "/%s/oauth2/v2.0/token"
                    tenant)
        scope (string/join " "
                           (filter (partial not= "offline_access")
                                   (map string/lower-case
                                        scopes)))
        secret (if (or (nil? client-secret)
                       (string/blank? client-secret))
                 {}
                 {:client_secret client-secret})
        base-params (merge {:client_id clientid
                            :scope scope
                            :redirect_uri (redirect-url server=config)}
                           secret)]
    {:url url
     :params (merge base-params
                    (if refresh?
                      {:grant_type "refresh_token"
                       :refresh_token code}
                      {:grant_type "authorization_code"
                       :code code}))}))

(defn tokens-from-token-response [body]
  {:token (get body "access_token")
   :refresh-token (get body "refresh_token")})
