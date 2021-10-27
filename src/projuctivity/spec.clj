(ns projuctivity.spec
  (:require [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]))

(defn non-empty-string? [s]
  (and (string? s) (not= s "")))

(defn gen-char-len [chargen min max]
  (gen/fmap str/join
            (gen/vector (chargen) min max)))
