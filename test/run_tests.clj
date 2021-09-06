(ns run-tests
  (:require  [clojure.test :as t]
             [clojure.spec.test.alpha :as spectest]
             [projuctivity.msgraph.core-test :as core-test]
             [projuctivity.msgraph.auth.urls-test :as auth-urls-test]
             [projuctivity.transform-test :as transform-test]
             [projuctivity.config-test :as config-test]))

(spectest/instrument)

(defn passed? [result]
  (every? zero?
          (map result [:fail :error])))

(defn -main [& args]
  (let [res (t/run-tests 'projuctivity.msgraph.core-test
                         'projuctivity.msgraph.auth.urls-test
                         'projuctivity.transform-test
                         'projuctivity.cache-test
                         'projuctivity.config-test)]
    (System/exit (if (passed? res)
                   0
                   1))))
