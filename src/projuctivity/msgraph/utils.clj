(ns projuctivity.msgraph.utils)

;; TODO: Move this up one level out of msgraph

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (read-string (slurp source)))

(defn save-edn
  [filename content]
  (let [formatted (with-out-str (pr content))]
    (spit filename formatted)))
