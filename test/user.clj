(ns user
  (:require [projuctivity.core :as proj]
            [clojure.spec.test.alpha :as stest]
            [clojure.java.io :as io]
            [projuctivity.cache.file]
            [projuctivity.cache.api :as cache-api])
  (:import [projuctivity.cache.file EDNFileCache]))

(stest/instrument)

(comment
(let [uri (java.net.URI. "https://larochelle.com")]
  (.getHost uri))

  ;; To see the config
  (projuctivity.config/calendar-config)

  ;; just testing the API retrieval
  (proj/events-between "2021-01-01" "2021-02-01")

  ;; testing full auth flow
  (let [file (io/file ".msgraph-cache.edn")]
    (if (.exists file)
      (io/delete-file file))
    (proj/events-between "2021-01-01" "2021-02-01")))

