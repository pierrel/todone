(ns projuctivity.msgraph.api
  (:require [clojure.spec.alpha :as s]
            [projuctivity.msgraph.core :as core]
            [projuctivity.api :as api]))

(s/def :projuctivity.msgraph.api/clientid string?)
(s/def :projuctivity.msgraph.api/tenant string?)
(s/def :projuctivity.msgraph.api/scope string?)
(s/def :projuctivity.msgraph.api/scopes
  (s/coll-of :projuctivity.msgraph.api/scope))
(s/def :projuctivity.msgraph.api/config
  (s/keys :req-un [:projuctivity.msgraph.api/clientid
                   :projuctivity.msgraph.api/scopes
                   :projuctivity.msgraph.api/tenant]))

;; TODO: move keystorepass and ssl_keystore somewhere else
(defrecord MSGraphUser [clientid tenant scopes keystorepass ssl_keystore]
  api/EventSearch
  (events [user date1 date2]
    (core/events-between user date1 date2))
  api/Auth
  (auth [user]
        (core/auth user)))

(def scopes ["user.read"
             "Tasks.ReadWrite"
             "Calendars.ReadWrite"])

(s/fdef user
  :args (s/cat :m map?)
  :ret :projuctivity.msgraph.api/config)
(defn user
  "Returns an `MSGraphUser` based on the given map `m`."
  ([m]
   (map->MSGraphUser (assoc m
                            :scopes scopes))))
