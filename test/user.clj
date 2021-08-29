(ns user
  (:require [projuctivity.core :as proj]
            [clojure.spec.test.alpha :as stest]))

(stest/instrument)

(comment
  (proj/events-between "2021-01-01" "2021-02-01"))

