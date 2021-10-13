(ns projuctivity.cache.api
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))


(defprotocol Cache
  (place [cache k v] "Set the `cache` key `k` to v")
  (retrieve [cache k] "Get the value for `k` from `cache`"))

(defrecord MemCache [mem]
  Cache
  (place [cache k v]
    (let [m (:mem cache)]
      (reset! m
              (assoc m k v))))
  (retrieve [cache k]
    (-> cache :mem k)))

(s/def :projuctivity.cache.api/cache
  (s/with-gen (partial satisfies? Cache)
    ;; TODO: see if this always returns a NEW MemCache. If not then re-write
    (gen/return (MemCache. (atom {})))))

(defn with-saved
  "Saves `v` to `k` using the API and returns `v`"
  [cache k v]
  (do
    (place cache k v)
    v))
