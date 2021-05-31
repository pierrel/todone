(ns projuctivity.msgraph.auth.pure-test
  (:use [helper])
  (:require [projuctivity.msgraph.auth.pure :as sut]
            [clojure.pprint :as pprint]
            [clojure.spec.test.alpha :as spectest]
            [clojure.test :as t]))

(t/deftest spec-checks
  (check-and-report-specs 'projuctivity.msgraph.auth.pure))
