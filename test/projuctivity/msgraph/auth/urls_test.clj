(ns projuctivity.msgraph.auth.urls-test
  (:require [clojure.test :as t]
            [projuctivity.msgraph.auth.urls :as suite]
            [helper :as helper]))

(t/deftest spec-checks
  (helper/check-and-test-specs 'projuctivity.msgraph.auth.urls))
