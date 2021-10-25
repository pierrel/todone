(ns projuctivity.cache-test
  (:require [projuctivity.cache.api :as api]
            [clojure.java.io :as io]
            [clojure.test :as t])
  (:import [projuctivity.cache.file EDNFileCache]))

(def filename "test-cache.edn")
(def cache (EDNFileCache. filename))

(defn delete-if-exists [filename]
  (let [file (io/file filename)]
    (if (.exists file)
      (io/delete-file file))))

(t/use-fixtures :once
  (fn [f]
    (delete-if-exists filename)
    (f)
    (delete-if-exists filename)))

(t/deftest empty-retrieve
  (delete-if-exists filename)
  (t/is (nil? (api/retrieve cache :test-key))))

(t/deftest put-then-retrieve
  (let [k :test-key
        v {:something "here"}]
    (api/place cache k v)
    (t/is (= v (api/retrieve cache k)))))

(t/deftest with-saved
  (let [k :other-test-key
        v {:hey "there"}]
    (t/is (= v (api/with-saved cache k v)))
    (t/is (= v (api/retrieve cache k)))))
