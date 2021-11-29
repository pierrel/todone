(ns user
  (:require [projuctivity.core :as proj]
            [clojure.spec.test.alpha :as stest]
            [clojure.java.io :as io]
            [projuctivity.cache.file]
            [projuctivity.cache.api :as cache-api]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen])
  (:import [projuctivity.cache.file EDNFileCache]))

;; THIS IS IMPORTANT
(stest/instrument)
(stest/instrument `projuctivity.config/load-config 
                  {:stub #{`projuctivity.config/load-config}})

(defn genit [k]
  (-> k s/gen gen/generate))

(comment
  ;; testing
  (let [config (genit :projuctivity.config/config)
        msexample (genit :projuctivity.config/msgraph)
        example-ret (genit (s/nilable (s/tuple :projuctivity.config/service-provider
                                               :projuctivity.config/service-config)))]
    {:example msexample
     :example-ret example-ret
     :actual (projuctivity.config.pure/service-part :calendar config)})

  (stest/summarize-results
   (stest/check 'projuctivity.config/tasks-config))

  ;; To see the config
  (projuctivity.config/calendar-config)

  ;; just testing the API retrieval
  (proj/events-between "2021-01-01" "2021-02-01")

  ;; testing full auth flow
  (let [file (io/file ".msgraph-cache.edn")]
    (if (.exists file)
      (io/delete-file file))
    (proj/events-between "2021-01-01" "2021-02-01")))

