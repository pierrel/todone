(ns helper
  (:require  [clojure.test :as t]
             [clojure.pprint :as pprint]
             [clojure.set :as setop]
             [clojure.spec.test.alpha :as spectest]))

(defn test-summary
  ([summary & summaries]
   (doseq [sum (conj summaries summary)]
     (test-summary sum)))
  ([summary]
   (t/is (or (zero? (:total summary))
             (= (:total summary)
                (:check-passed summary))))))

(defn check-and-report-specs
  "Checks all of `namespace` without `to-stub` and returns
  a summary of each individual spec check."
  [namespace & {:keys [to-stub]}]
  (let [all-fns (spectest/enumerate-namespace namespace)
        to-check (setop/difference all-fns to-stub)]
    (spectest/instrument to-check)
    (doseq [stub to-stub]
      (spectest/instrument stub {:stub #{stub}}))
    (map (comp spectest/summarize-results spectest/check)
         to-check)))

(def check-and-test-specs (comp (partial apply test-summary)
                                check-and-report-specs))
