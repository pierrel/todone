(ns run-tests
  (:require  [clojure.test :as t]
             [projuctivity.msgraph.core-test :as core-test]
             [projuctivity.msgraph.auth.pure-test :as auth-pure-test]))

(defn passed? [result]
  (every? zero?
          (map result [:fail :error])))

(defn -main [& args]
  (let [res (t/run-tests 'projuctivity.msgraph.core-test
                         'projuctivity.msgraph.auth.pure-test)]
    (System/exit (if (passed? res)
                   0
                   1))))
