(ns projuctivity.msgraph.auth.urls-test
  (:require [clojure.test :as t]
            [helper :as helper]))

(t/deftest spec-checks
  (helper/check-and-report-specs 'projuctivity.msgraph.auth.urls))
