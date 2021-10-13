(ns projuctivity.msgraph.auth
  "Auth workflows."
  (:require [ring.adapter.jetty :as server]
            [projuctivity.request.api :as request-api]
            [projuctivity.request.core :as request]
            [clojure.core.async :as async]
            [ring.util.response :as resp]
            [ring.middleware.ssl :as ssl]
            [projuctivity.cache.api :as cache-api]
            [projuctivity.cache.file]
            [clojure.spec.alpha :as s]
            [projuctivity.msgraph.auth.urls :as urls])
  (:import [projuctivity.cache.file EDNFileCache]
           [projuctivity.request.core JSONService]))

(s/def :auth/clientid string?)
(s/def :auth/tenant string?)
(s/def :auth/keystorepass string?)
(s/def :auth/ssl-keystore string?)
(s/def :auth/config (s/keys :req-un [:auth/clientid
                                     :auth/tenant
                                     :auth/keystorepass
                                     :auth/ssl-keystore
                                     :auth/scopes]))

(s/def :auth/token string?)
(s/def :auth/refresh-token string?)
(s/def :auth/tokens (s/keys :req-un [:auth/token :auth/refresh-token]))

(def default-cache (EDNFileCache. ".msgraph-cache.edn"))
(def service (JSONService. "https://login.microsoftonline.com"))

(defonce server-debug (atom nil))

(defn jetty-opts [keystore-pass]
  {:port         urls/port
   :ssl?          true
   :keystore "keystore.jks"
   :key-password keystore-pass
   :join?        false})

(s/fdef code-from-channel
  :ret string?)
(defn- code-from-channel
  "Waits for the code to be passed to the channel.
  Closes the channel, stops the server and returns the code."
  [channel server]
  (let [code (async/<!! channel)]
    (async/close! channel)
    (.stop server)
    code))

(defn- carry-code [channel code]
  (async/go (async/>! channel code)))

(defn handler [clientid tenant scopes f request]
  (try
    (case (:uri request)
      "/token"
      (if-let [code (last (re-matches #".*code=([^&]+).*$"
                                      (get request :query-string "")))]
        (do
          (f code)
          (resp/response "Authorization complete, you can now close this browser tab/window."))
      ;; TODO: implement error recovery: https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow#error-response
        (resp/response "Could not get code. This endpoint should only be redirected-to by MS. Use /auth to get started."))

      "/testing"
      (resp/response "This is just a test")

      "/stop"
      (do
        (f "stop")
        (resp/response "Stopping"))

      "/error"
      (throw (ex-info "Testing out the error handling."
                      {:source "error endpoint."}))

      "/auth"
      (let [dest (urls/ms-auth-url clientid tenant scopes)]
        (resp/redirect dest))

      ;; default
      (resp/response "Please go to /auth for authentication or /stop to stop the server."))

    ;; Let the channel know there was an error
    (catch RuntimeException e
      (f "error")
      (throw e))))

(s/fdef get-tokens-from-code
  :args (s/cat :code string?
               :tenant string?
               :clientid string?
               :client-secret string?
               :scopes (s/spec :auth/scopes))
  :ret (s/spec :auth/tokens))
(defn get-tokens-from-code [code tenant clientid client-secret scopes]
  (let [{:keys [url params]} (urls/token-request-params code
                                                        tenant
                                                        clientid
                                                        client-secret
                                                        scopes
                                                        false)]
    (urls/tokens-from-token-response
     (request-api/post service
                       url
                       {:accept :json
                        :content-type :application/x-www-form-urlencoded
                        :form-params params}))))

(s/fdef get-tokens-from-refresh-token
  :args (s/cat :refresh-token string?
               :tenant string?
               :clientid string?
               :scopes (s/spec :auth/scopes))
  :ret (s/spec :auth/tokens))
(defn get-tokens-from-refresh-token [refresh-token tenant clientid scopes]
  (let [{:keys [url params]} (urls/token-request-params refresh-token
                                                        tenant
                                                        clientid
                                                        scopes
                                                        true)]
    (urls/tokens-from-token-response
     (request-api/post service
                       url
                       {:accept :json
                        :content-type :application/x-www-form-urlencoded
                        :form-params params}))))
(s/fdef get-code
  :args (s/cat :config (s/spec :auth/config)
               :scopes (s/spec :auth/scopes))
  :ret string?)
;; TODO: pass in keystore filename instead of hardcoding it in @jetty-opts
(defn get-code [config scopes]
  (println (format "Running server to request credentials.\nPlease head over to %s" urls/auth-url))
  (let [{:keys [clientid
                tenant
                ssl-keystore
                keystorepass]} config
        c                      (async/chan)
        the-handler            (-> (partial handler
                                            clientid
                                            tenant
                                            scopes
                                            (partial carry-code c))
                                   ssl/wrap-hsts
                                   ssl/wrap-ssl-redirect)
        opts                   {:port         3000
                                :ssl?         true
                                :keystore     ssl-keystore
                                :key-password keystorepass
                                :join?        false}
        s                      (server/run-jetty the-handler opts)]
    (reset! server-debug s)
    (code-from-channel c s)))

(s/fdef get-tokens
  :args (s/alt :unary (s/cat :config (s/spec :auth/config))
               :binary (s/cat :config (s/spec :auth/config)
                              :cache (s/spec :projuctivity.cache.api/cache)))
  :ret (s/spec :auth/tokens))
(defn get-tokens
  ([config cache]
   (let [{:keys [clientid
                 client-secret
                 tenant
                 scopes]
          :or   {client-secret ""}} config
         all-scopes                 (conj scopes "offline_access")
         code                       (get-code config scopes)]
     (case code
       "stop"  (throw (ex-info "Auth process manually stopped." {}))
       "error" (throw (ex-info "Auth process stopped due to an error." {}))
       (do
         (println "got code" code)
         (cache-api/with-saved cache :tokens
           (get-tokens-from-code code
                                 tenant
                                 clientid
                                 client-secret
                                 all-scopes))))))
  ([config]
   (get-tokens config default-cache)))

(s/fdef tokens
  :args (s/alt :unary (s/cat :config (s/spec :auth/config))
               :binary (s/cat :config (s/spec :auth/config)
                              :cache (s/spec :projuctivity.cache.api/cache)))
  :ret (s/spec :auth/token))
(defn tokens
  ([config cache]
   (if-let [tokens-map (cache-api/retrieve cache :tokens)]
     tokens-map
     (get-tokens config)))
  ([config]
   (tokens config default-cache)))

(s/fdef refresh-token
  :args (s/alt :unary (s/cat :config (s/spec :auth/config))
               :binary (s/cat :config (s/spec :auth/config)
                              :refresh-token (s/spec :auth/refresh-token)))
  :ret (s/spec :auth/tokens))
(defn refresh-token
  ([config refresh-token]
   (let [{:keys [clientid tenant scopes]} config]
     (cache-api/with-saved default-cache :tokens
       (let [tokens (get-tokens-from-refresh-token refresh-token
                                                   tenant
                                                   clientid
                                                   scopes)]
         (if (nil? (:refresh-token tokens))
           (assoc tokens :refresh-token refresh-token)
           tokens)))
     ([config]
      (refresh-token config
                     (:refresh-token (tokens config)))))))
