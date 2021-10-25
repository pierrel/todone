(ns projuctivity.msgraph.auth-test
  (:require [projuctivity.msgraph.auth :as sut]
            [projuctivity.msgraph.auth.urls :as urls]
            [clj-http.client :as http]
            [projuctivity.request.api :as request-api]
            [clojure.test :as t]
            [clojure.spec.test.alpha :as spectest]
            [projuctivity.cache.api :as cache-api]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.alpha :as s]
            [helper :as helper]
            [clojure.core.async :as async]
            [projuctivity.config :as config]
            [ring.util.response :as resp]
            [ring.adapter.jetty :as server]))

(def saved-server (atom nil))

(defn ms-auth-handler [local-base-url code request]
  (resp/redirect (format "%s/token?code=%s" local-base-url code)))

(defn ms-code-handler [tokens method hostname resource params]
  (if (and (= "https://login.microsoftonline.com" hostname)
           (re-matches #".*/oauth/v2.0/token.*" resource))
    {"access_token" (:token tokens)
     "refresh_token" (:refresh-token tokens)}
    (throw (ex-info "ms-code-handler called with wrong params"
                    {:called-hostname hostname
                     :called-resource resource}))))

(defn create-keystore [path pass]
  (let [dname "CN=localhost, OU=ID, O=IBM, L=Hursley, S=Hants, C=GB"]
    (clojure.java.shell/sh "keytool"
                           "-genkey"
                           "-noprompt"
                           "-dname" dname
                           "-alias" "ssl"
                           "-keyalg" "RSA"
                           "-sigalg" "SHA256withRSA"
                           "-validity" "365"
                           "-keystore" path
                           "-storepass" pass
                           "-keypass" pass)))

(defn doitall []
  (let [config (-> :auth/config s/gen gen/generate)
        cache (-> :projuctivity.cache.api/cache s/gen gen/generate)
        expected-tokens (-> :auth/tokens s/gen gen/generate)
        code (-> string? s/gen gen/generate)
        server-url (urls/base-url)
        ms-auth-server (server/run-jetty (partial ms-auth-handler
                                                  server-url
                                                  code)
                                         {:port 3001
                                          :join? false})
        keystore-path (:ssl-keystore config)
        keystore-pass (:keystorepass config)]
    (reset! saved-server ms-auth-server) ;; for debugging
    (projuctivity.request.core/inject-handler! (partial ms-code-handler
                                                expected-tokens))
    (println (create-keystore keystore-path keystore-pass))
    (let [c (async/thread (sut/get-tokens config cache))
          _ (Thread/sleep 2000) ;; let the server start up
          response (http/get (format "%s/auth" server-url)
                             {:query-params {:test true}
                              :insecure? true})
          auth-ret (:body response)]
      (let [received-tokens (async/<!! c)]
        (projuctivity.request.core/inject-handler! nil)
        (.stop ms-auth-server)
        (clojure.java.io/delete-file keystore-path true)
        [received-tokens expected-tokens auth-ret]))))

(comment
  ;; test it
  (let [[received-tokens expected-tokens auth-ret] (doitall)]
    (.stop @projuctivity.msgraph.auth/server-debug)
    (.stop @saved-server)
    (println "GOT THIS" received-tokens expected-tokens auth-ret))

  (do
    (.stop @projuctivity.msgraph.auth/server-debug)
    (.stop @saved-server))

  (condp re-matches "/auth/"
    #"/auth(|/)" "got it"
    "nothing"))

(t/deftest get-tokenss
  (let [[c expected-tokens ms-auth-server] (doitall)
        received-tokens (async/<!! c)]
    (.stop ms-auth-server)
    (t/is (= expected-tokens received-tokens))))
