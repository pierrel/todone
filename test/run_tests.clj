(ns run-tests
  (:require  [clojure.test :as t]
             [projuctivity.msgraph.core-test :as core-test]
             [projuctivity.msgraph.auth.pure-test :as auth-pure-test]))

(defn -main [& args]
  (t/run-tests 'projuctivity.msgraph.core-test
               'projuctivity.msgraph.auth.pure-test))

