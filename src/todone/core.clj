(ns todone.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clj-http.client :as http]
            [msgraph.auth :as auth]))

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



