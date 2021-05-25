(ns projuctivity.msgraph.utils)


(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (read-string (slurp source)))

(defn save-edn
  [filename content]
  (let [formatted (with-out-str (pr content))]
    (spit filename formatted)))
