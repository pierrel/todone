(ns projuctivity.core.pure
  (:require [java-time :as t]
            [clojure.spec.alpha :as s]))

(s/fdef is-time?
  :ret boolean?)
(defn is-time? [date]
  (t/local-date-time? date))

(s/fdef str-to-time
  :args (s/cat :date-str string?)
  :ret is-time?)
(defn str-to-time [date-str]
  (if (re-find #"T" date-str)
    (t/local-date-time date-str)
    (t/local-date-time (format "%sT00:00:00" date-str))))