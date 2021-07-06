(ns projuctivity.transform)

(defn transform-using
  "Transforms map `m` according to transformer `t`"
  [t m]
  (reduce (fn [acc [k plan]]
            (assoc acc
                   k
                   (cond
                     (vector? plan) (get-in m plan)
                     (fn? plan) (plan m)
                     (or (string? plan)
                         (keyword? plan)) (get m plan))))
          {}
          t))

(defmacro deft
  "TODO: use and test me."
  [name plan]
  `(def ~name
     (partial transform-using ~plan)))
