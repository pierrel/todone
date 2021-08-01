(ns projuctivity.msgraph.auth
  "Auth workflows."
  (:require [ring.adapter.jetty :as server]
            [projuctivity.msgraph.utils :as utils]
            [clojure.java.io :as io]
            [clojure.core.async :as async]
            [ring.util.response :as resp]
            [clj-http.client :as http]
            [clojure.spec.alpha :as s]
            [projuctivity.msgraph.auth.urls :as urls]))

(s/def :auth/clientid string?)
(s/def :auth/tenant string?)
(s/def :auth/keystorepass string?)
(s/def :auth/ssl_keystore string?)
(s/def :auth/config (s/keys :req-un [:auth/clientid
                                     :auth/tenant
                                     :auth/keystorepass
                                     :auth/ssl_keystore
                                     :auth/scopes]))

(s/def :auth/token string?)
(s/def :auth/refresh-token string?)
(s/def :auth/tokens (s/keys :req-un [:auth/token :auth/refresh-token]))

(def tokens-filename ".msgraph-tokens.edn")

;; This is set whenever the server is started.
;; Mostly for debug purposes.
(def server-debug (atom nil))

(def on-codespaces?
  (urls/is-codespaces? urls/on-device-hostname))

(defn- with-saved
  "Saves the content to the temp file and returns the content."
  [filename content]
  (utils/save-edn filename content)
  content)

(defn jetty-opts [keystore-pass]
  (let [init {:port urls/port
              :join? false}]
    (if on-codespaces?
      init
      (merge init
             {:ssl? true
              :keystore "keystore.jks"
              :key-password keystore-pass}))))
 
(s/fdef code-from-channel
  :args (s/cat :channel (s/and (partial instance?
                                        clojure.core.async.impl.protocols/Channel)
                               (partial instance?
                                        clojure.core.async.impl.protocols/ReadPort))
               :server (partial instance?
                                org.eclipse.jetty.util.component.LifeCycle))
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
  (case (:uri request)
    "/token"
    (if-let [code (last (re-matches #".*code=([^&]+).*$"
                                    (get request :query-string "")))]
      (f code)
      ;; TODO: implement error recovery: https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow#error-response
      (resp/response "Could not get code. This endpoint should only be redirected-to by MS. Use /auth to get started."))

    "/testing"
    (resp/response "This is just a test")
    
    ;; default
    (resp/redirect (urls/ms-auth-url clientid tenant scopes))))

(s/fdef get-tokens-from-code
  :args (s/cat :code string?
               :tenant string?
               :clientid string?
               :scopes (s/spec :auth/scopes))
  :ret (s/spec :auth/tokens))
(defn get-tokens-from-code [code tenant clientid scopes]
  (let [{:keys [url params]} (urls/token-request-params code
                                                        tenant
                                                        clientid
                                                        scopes
                                                        false)]
    (urls/tokens-from-token-response
     (http/post url
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
     (http/post url
                {:accept :json
                 :content-type :application/x-www-form-urlencoded
                 :form-params params}))))
(s/fdef get-code
  :args (s/cat :clientid string?
               :tenant string?
               :scopes (s/spec :auth/scopes)
               :keystore-pass string?)
  :ret string?)
(defn get-code [clientid tenant scopes keystore-pass]
  (println (format "Running server to request credentials. Please head over to %s" urls/auth-url))
  (let [c (async/chan)
        s (server/run-jetty (partial handler
                                     clientid
                                     tenant
                                     scopes
                                     (partial carry-code c))
                            (jetty-opts keystore-pass))]
    (reset! server-debug s)
    (code-from-channel c s)))

(s/fdef get-tokens
  :args (s/cat :config (s/spec :auth/config))
  :ret (s/spec :auth/tokens))
(defn get-tokens [config]
  (let [{:auth/keys [clientid tenant scopes keystorepass]} config
        all-scopes (conj scopes "offline_access")
        code (get-code clientid tenant all-scopes keystorepass)]
    (println "got code" code)
    (with-saved tokens-filename
      (get-tokens-from-code code
                            tenant
                            clientid
                            all-scopes))))

(s/fdef token
  :args (s/cat :config (s/spec :auth/config))
  :ret (s/spec :auth/token))
(defn token [config]
  (:token
   (if (.exists (io/file tokens-filename))
     (utils/load-edn tokens-filename)
     (get-tokens config))))

(s/fdef refresh-token
  :args (s/cat :config (s/spec :auth/config)
               :refresh-token (s/spec :auth/refresh-token))
  :ret (s/spec :auth/tokens))
(defn refresh-token
  ([config refresh-token]
   (let [{:keys [clientid tenant scopes]} config]
     (with-saved tokens-filename
       (let [tokens (get-tokens-from-refresh-token refresh-token
                                                   tenant
                                                   clientid
                                                   scopes)]
         (if (nil? (:refresh-token tokens))
           (assoc tokens :refresh-token refresh-token)
           tokens)))))
  ([config]
   (refresh-token config
                  (:refresh-token (token config)))))