(ns projuctivity.core
  (:use [projuctivity.models])
  (:require [projuctivity.msgraph.api :as ms]
            [projuctivity.api :as api]
            [projuctivity.config :as config]
            [java-time :as t]))

(def calendar-user (atom nil))
(def tasks-user (atom nil))

(defn assign-users!
  "Assigns users based on the config."
  []
  (let [server-config (config/server-config)]
    (doseq [[user config-fn] [[calendar-user config/calendar-config]
                              [tasks-user config/tasks-config]]]
      (let [[service user-config] (config-fn)]
        (case service
          :msgraph (reset! user (ms/user (merge user-config
                                                server-config))))))))

(defn check-and-assign!
  "Assigns users if they have not already been assigned."
  []
  (if (some nil? [@calendar-user
                  @tasks-user])
    (assign-users!)))

(defn auth []
  (check-and-assign!)
  (api/auth @calendar-user))

(defn events
  "Returns a lazy list of `Event`s between date1 and date2"
  [date1 date2]
  (check-and-assign!)
  (api/events @calendar-user date1 date2))

(defn open-tasks
  "Returns a lazy list of open `Task`s"
  []
  (check-and-assign!)
  (api/open-tasks @tasks-user))
