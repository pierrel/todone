(ns projuctivity.cache.api)

(defprotocol Cache
  (place [cache k v] "Set the `cache` key `k` to v")
  (retrieve [cache k] "Get the value for `k` from `cache`")
  (with-saved [cache k v] "Returns `v` after saving it to `cache`"))
