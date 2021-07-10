(ns projuctivity.msgraph.api
  (:use [projuctivity.api]
        [projuctivity.msgraph.core])
  (:require [clj-http.client :as http]
            [clojure.spec.alpha :as s]))

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
  EventSearch
  (events [user date1 date2]
    (events-between user date1 date2)))

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
