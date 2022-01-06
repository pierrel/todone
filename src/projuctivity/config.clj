(ns projuctivity.config
  "Reads and organizes the config file."
  (:use [projuctivity.config.pure])
  (:require [clojure.spec.alpha :as s]
            [projuctivity.msgraph.utils :as utils]))

(s/def
  :projuctivity.config/multiarity-config-input
  (s/alt :nullary (s/cat)
         :unary (s/cat :config :projuctivity.config/config)))
(s/def ::multiarity-config-file-input
       (s/alt :nullary (s/cat)
              :unary (s/cat :config-filename string?)))

(def filename ".config.edn")

(s/fdef load-config
  :args ::multiarity-config-file-input
  :ret ::config)
(defn load-config
  ([config-filename]
   (utils/load-edn config-filename))
  ([]
   (load-config filename)))

;; TODO: must move the auth-related server config into the service key
(s/fdef server-config
  :args :projuctivity.config/multiarity-config-input
  :ret :projuctivity.config/server)
(defn server-config
  "Returns the server configuration used to handle oath."
  ([config]
   (:server config))
  ([]
   (server-config (load-config))))

(s/fdef calendar-config
  :args :projuctivity.config/multiarity-config-input
  :ret :projuctivity.config/service-part-ret)
(defn calendar-config
  "Returns the calendar service part of the config, including the service provider."
  ([config]
   (service-part :calendar config))
  ([]
   (calendar-config (load-config))))

(s/fdef tasks-config
  :args :projuctivity.config/multiarity-config-input
  :ret :projuctivity.config/service-part-ret)
(defn tasks-config
  "Returns the tasks service part of the config, including the service provider."
  ([config]
   (service-part :tasks config))
  ([]
   (tasks-config (load-config))))
