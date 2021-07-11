(ns projuctivity.msgraph.core-test
  (:use [helper])
  (:require [projuctivity.msgraph.core :as sut]
            [clojure.spec.test.alpha :as spectest]
            [clojure.test :as t]))

(spectest/instrument `sut/httpget {:stub #{`sut/httpget}})

(t/deftest spec-checks
  (let [results (spectest/summarize-results
                 (spectest/check 'sut/get-resource
                                 'sut/httpget))]))
