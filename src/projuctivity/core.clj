(ns projuctivity.core
  (:require [projuctivity.config :as config]
            [projuctivity.msgraph.api :as ms]
            [projuctivity.api :as api]
            [projuctivity.core.pure :as pure]
            [clojure.spec.alpha :as s]
            [projuctivity.models :as models])
  (:import [projuctivity.models Event Task]))

(def calendar-user (atom nil))
(def tasks-user (atom nil))

(def users [calendar-user
            tasks-user])

(defn assign-users!
  "Assigns users based on the config."
  ([configuration]
   (let [server-config (config/server-config configuration)]
     (doseq [[user [service user-config]] [[calendar-user (config/calendar-config configuration)]
                                           [tasks-user (config/tasks-config configuration)]]]
       (case service
         :msgraph (reset! user (ms/user (merge user-config
                                               server-config)))))))
  ([]
   (assign-users! (config/load-config))))

(defn check-and-assign!
  "Assigns users if they have not already been assigned."
  []
  (if (some nil? [@calendar-user
                  @tasks-user])
    (assign-users!)))

(defn auth-all []
  (check-and-assign!)
  (let [user-refs (set (map deref users))]
    (doseq [user-ref user-refs]
      (api/auth user-ref))))

(s/fdef events-between
  :args (s/cat :date1 (s/or :time pure/is-time?
                            :string string?)
               :date2 (s/or :time pure/is-time?
                            :string string?))
  :ret (s/coll-of (partial instance? Event)))
(defn events-between
  "Returns a lazy list of `Event`s between date1 and date2.
   
   Converts dates to the correct format if necessary."
  [date1 date2]
  (check-and-assign!)
  (let [[d1 d2] (map #(if (pure/is-time? %)
                        %
                        (pure/str-to-time %))
                     [date1 date2])]
    (api/events @calendar-user d1 d2)))

(s/fdef open-tasks
        :ret (s/coll-of (partial instance? Task)))
(defn open-tasks
  "Returns a lazy list of open `Task`s"
  []
  (check-and-assign!)
  (api/open-tasks @tasks-user))