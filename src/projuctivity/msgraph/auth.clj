(ns projuctivity.msgraph.auth
  "High-level workflows that use pure functions in auth.pure."
  (:use [projuctivity.msgraph.auth.pure])
  (:require [ring.adapter.jetty :as server]
            [clojure.core.async :as async]
            [ring.util.response :as resp]
            [clj-http.client :as http]))

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
      (resp/response "Could not get code. This endpoint should only be redirected-to by MS. Use /auth to get started."))

    ;; default
    (resp/redirect (ms-auth-url clientid tenant scopes))))

(defn get-tokens-from-code [code tenant clientid scopes]
  (let [{:keys [url params]} (token-request-params code
                                                   tenant
                                                   clientid
                                                   scopes)]
    (tokens-from-token-response
     (http/post url
                {:accept :json
                 :content-type :application/x-www-form-urlencoded
                 :form-params params}))))

(defn get-code [clientid tenant scopes keystore-pass]
  (println (format "Running server to request credentials. Please head over to %s" auth-url))
  (let [c (async/chan)
        s (server/run-jetty (partial handler
                                     clientid
                                     tenant
                                     scopes
                                     (partial carry-code c))
                            {:port port
                             :join? false
                             :ssl? true
                             :keystore "keystore.jks"
                             :key-password keystore-pass})]
    (code-from-channel c s)))

(defn get-tokens [config]
  (let [{:keys [clientid tenant scopes keystorepass]} config
        all-scopes (conj scopes "offline_access")
        code (get-code clientid tenant all-scopes keystorepass)]
    (println "got code" code)
    (get-tokens-from-code code
                          tenant
                          clientid
                          all-scopes)))
