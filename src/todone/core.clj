(ns todone.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clj-http.client :as http]
            [msgraph.auth :as auth]))

(def authendpoint "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize")

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (try
    (with-open [r (io/reader source)]
      (edn/read (java.io.PushbackReader. r)))

    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))))

(defn get-token []
  (let [config (load-edn ".config.edn")
        token (auth/get-token (:appid config)
                              "consumers"
                              ["offline_access"
                               "user.read"]
                              (:keystorepass config))]
    token))


(defn -main [& args]
  (let [config (load-edn ".config.edn")
        res (http/get authendpoint
                      {:query-params {"client_id" (:appid config)
                                      "response-type" "code"
                                      "scope" "offline_access user.read"
                                      "redirect_uri" "https://login.microsoftonline.com/common/oauth2/nativeclient"}})]
    (println res)
    (println (:appid config))))

