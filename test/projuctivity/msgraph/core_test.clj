(ns projuctivity.msgraph.core-test
  (:use [helper])
  (:require [projuctivity.msgraph.core :as sut]
            [clojure.spec.test.alpha :as spectest]
            [clojure.test :as t]))

(t/deftest spec-checks
  (spectest/instrument `sut/httpget {:stub #{`sut/httpget}})
  (spectest/instrument `sut/get-resource)

  (let [results (spectest/summarize-results
                 (spectest/check `sut/get-resource
                                 `sut/httpget))]))
