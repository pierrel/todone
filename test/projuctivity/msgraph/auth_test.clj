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
            [clojure.java.shell :as shell]
            [helper :as helper]
            [clojure.core.async :as async]
            [projuctivity.config :as config]
            [ring.util.response :as resp]
            [ring.adapter.jetty :as server]))

(defrecord TestCache []
  cache-api/Cache
  (place [cache k v]
    nil)
  (retrieve [cache k]
    nil))

(defn ms-auth-handler [local-base-url code request]
  (resp/redirect (format "%s/token?code=%s" local-base-url code)))

(defn ms-code-handler [tokens method hostname resource params]
  (if (and (= "https://login.microsoftonline.com" hostname)
           (re-matches #".*/oauth2/v2.0/token.*" resource))
    {"access_token"  (:token tokens)
     "refresh_token" (:refresh-token tokens)}
    (throw (ex-info "ms-code-handler called with wrong params"
                    {:called-hostname hostname
                     :called-resource resource}))))

(defn create-keystore [path pass]
  (let [dname "CN=localhost, OU=ID, O=IBM, L=Hursley, S=Hants, C=GB"]
    (shell/sh "keytool"
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

(defn auth-request
  "Handling the request to auth endpoint.
  Should do things like retry and the like"
  [url]
  (try
    (http/get (format "%s/auth" url)
              {:query-params {:test true}
               :insecure?    true})
    (catch Exception e
      (println "Got exception when requesting auth endpoit from test" e)
      (throw e))))

(t/deftest get-tokenss
  (let [config          (-> :auth/config s/gen gen/generate)
        cache           (TestCache.)
        expected-tokens (-> :auth/tokens s/gen gen/generate)
        code            (-> (s/and string?
                                   (complement clojure.string/blank?))
                            s/gen
                            gen/generate)
        server-url      (urls/base-url)
        ms-auth-server  (server/run-jetty (partial ms-auth-handler
                                                   server-url
                                                   code)
                                          {:port  3001
                                           :join? false})
        keystore-path   (:ssl-keystore config)
        keystore-pass   (:keystorepass config)]
    (projuctivity.request.core/inject-handler! (partial ms-code-handler
                                                        expected-tokens))
    (println (create-keystore keystore-path keystore-pass))
    (let [c        (async/thread (sut/get-tokens config cache))
          _        (Thread/sleep 5000) ;; let the server start up
          response (auth-request server-url)
          auth-ret (:body response)]
      (let [received-tokens (async/<!! c)]
        (projuctivity.request.core/inject-handler! nil)
        (.stop ms-auth-server)
        (clojure.java.io/delete-file keystore-path true)
        (t/is (= expected-tokens received-tokens))))))
