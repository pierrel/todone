(ns helper
  (:require  [clojure.test :as t]
             [clojure.pprint :as pprint]
             [clojure.spec.test.alpha :as spectest]))

(defn check-and-report-specs [namespace & {:keys [to-stub]}]
  (let [stub-lookup (zipmap to-stub (repeat true))]
    (doseq [fun (spectest/enumerate-namespace namespace)]
      (if (get stub-lookup fun)
        (spectest/instrument fun {:stub #{fun}})
        (spectest/instrument fun))
      (let [result (spectest/check fun)
            summary (spectest/summarize-results result)]
        (pprint/write summary :stream nil)
        (t/is (or (zero? (:total summary))
                  (= (:total summary)
                     (:check-passed summary))))))))
