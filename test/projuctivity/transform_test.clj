(ns projuctivity.transform-test
  (:require [projuctivity.transform :as sut]
            [clojure.test :as t]))

(t/deftest transform
  (let [example {:one "one"
                 :two {:one "two"}
                 :three 1}
        plan {:uno :one
              :dos [:two :one]
              :tres #(+ 2 (get % :three))}]
    (t/is (= {:uno "one"
              :dos "two"
              :tres 3}
             (sut/transform-using plan example)))))
