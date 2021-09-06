(ns projuctivity.cache.file
  (:require [projuctivity.cache.api :as api]
            [projuctivity.msgraph.utils :as utils]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]))

(defn- is-edn? [filename]
  (re-find #"\.edn$" filename))
(defn- is-read-writable? [filename]
  (let [file (io/file filename)]
    (and (.exists file)
         (.canRead file)
         (.canWrite file))))

(defn edn-file-contents
  "Returns the contents of edn file at `filename` and an empty
  empty map otherwise."
  [filename]
  (if (.exists (io/file filename))
    (utils/load-edn filename)
    {}))

(s/fdef EDNFileCache.
  :args (s/cat :filename (s/and string?
                                is-edn?
                                is-read-writable?)))
(defrecord EDNFileCache [filename]
  api/Cache
  (retrieve [c k]
    (let [filename (:filename c)]
      (if (.exists (io/file filename))
        (get (utils/load-edn filename)
             k))))
  (place [c k v]
    (let [file (:filename c)
          new-contents (assoc (edn-file-contents file)
                         k
                         v)]
      (utils/save-edn file new-contents)))
  (with-saved [c k v]
    (api/place c k v)
    v))

