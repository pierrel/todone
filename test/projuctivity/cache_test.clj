(ns projuctivity.cache-test
  (:require [projuctivity.cache.api :as api]
            [clojure.java.io :as io]
            [clojure.test :as t])
  (:import [projuctivity.cache.file EDNFileCache]))

(def filename "test-cache.edn")

(defn delete-if-exists [filename]
  (let [file (io/file filename)]
    (if (.exists file)
      (io/delete-file file))))

(t/use-fixtures :once
  (fn [f]
    (delete-if-exists filename)
    (f)))

(t/deftest edn-file-cache
  (let [cache (EDNFileCache. filename)
        k :test-key
        v {:something "here"}]
    (t/is (nil? (api/retrieve cache :test-key)))

    (do
      (api/place cache k v)
      (t/is (.exists (io/file filename)))
      (t/is (= v (api/retrieve cache k))))))
