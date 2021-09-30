(ns run-tests
  (:require  [clojure.test :as t]
             [clojure.spec.test.alpha :as spectest]
             [cognitect.test-runner.api :as test-runner]))

(defn run-tests [& opts]
  (spectest/instrument)
  (apply test-runner/test opts))
