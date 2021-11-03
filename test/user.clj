(ns user
  (:require [projuctivity.core :as proj]
            [clojure.spec.test.alpha :as stest]
            [clojure.java.io :as io]
            [projuctivity.cache.file]
            [projuctivity.cache.api :as cache-api]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen])
  (:import [projuctivity.cache.file EDNFileCache]))

(stest/instrument)

(defn genit [k]
  (-> k s/gen gen/generate))

(comment
  ;; testing
  (let [config (genit :projuctivity.config/config)
        msexample (genit :projuctivity.config/msgraph)]
    (projuctivity.config.pure/service-part :calendar config))
  (stest/abbrev-result
   (first (stest/check 'projuctivity.config.pure/service-part)))
  (stest/summarize-results
   (stest/check 'projuctivity.config.pure/service-part))

  ;; To see the config
  (projuctivity.config/calendar-config)

  ;; just testing the API retrieval
  (proj/events-between "2021-01-01" "2021-02-01")

  ;; testing full auth flow
  (let [file (io/file ".msgraph-cache.edn")]
    (if (.exists file)
      (io/delete-file file))
    (proj/events-between "2021-01-01" "2021-02-01")))

