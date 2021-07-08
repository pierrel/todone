(ns projuctivity.config
  "Reads and organizes the config file."
  (:use [projuctivity.config.pure])
  (:require [clojure.spec.alpha :as s]))

(s/def
  :projuctivity.config/multiarity-config-input
  (s/alt :nullary (s/cat)
         :unary (s/cat :config :projuctivity.config/config)))

(def filename ".configv2.edn")

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource).
  TODO: Move to utils."
  [source]
  (read-string (slurp source)))

(def config (load-edn filename))

(s/fdef server-config
  :args :projuctivity.config/multiarity-config-input
  :ret :projuctivity.config/server)
(defn server-config
  "Returns the server configuration used to handle oath."
  ([config]
   (:server config))
  ([]
   (server-config config)))

(s/fdef calendar-config
  :args :projuctivity.config/multiarity-config-input
  :ret (s/tuple keyword? :projuctivity.config/service-config))
(defn calendar-config
  "Returns the calendar service part of the config, including the service provider."
  ([config]
   (service-part :calendar config))
  ([]
   (calendar-config config)))

(s/fdef tasks-config
  :args :projuctivity.config/multiarity-config-input
  :ret (s/tuple keyword? :projuctivity.config/service-config))
(defn tasks-config
  "Returns the tasks service part of the config, inclufing the service provider."
  ([config]
   (service-part :tasks config))
  ([]
   (tasks-config config)))
