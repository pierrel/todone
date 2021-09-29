(ns projuctivity.cache.api)

(defprotocol Cache
  (place [cache k v] "Set the `cache` key `k` to v")
  (retrieve [cache k] "Get the value for `k` from `cache`"))

(defn with-saved
  "Saves `v` to `k` using the API and returns `v`"
  [cache k v]
  (do
    (place cache k v)
    v))
