(ns projuctivity.msgraph.pure
  (:require [projuctivity.models :as models]
            [projuctivity.transform :as trans]
            [java-time :as t]
            [clojure.spec.alpha :as s]
            [cheshire.core :as json]))

(s/fdef config-params
  :args (s/cat :config :projuctivity.msgraph.api/config)
  :ret map?)
(defn config-params
  "Takes the config and turns them into necessary query params."
  [config]
  (into {}
        (for [[k v] (select-keys config [:client-secret])]
          [(clojure.string/replace (name k) "-" "_")
           v])))

(defn event-time-str
  "Takes a `key` and ms calendar event map `m` and returns the time string."
  [key m]
  (str (let [dt (get-in m [key "dateTime"])
             z (get-in m [key "timeZone"])]
         (t/zoned-date-time (t/local-date-time dt) z))))

(def event-transformer
  (partial trans/transform-using
           {:id "id"
            :subject "subject"
            :body ["body" "content"]
            :link "webLink"
            :start (partial event-time-str "start")
            :end (partial event-time-str "end")
            :organizer ["organizer" "emailAddress" "address"]}))

(defn to-event [resp-map]
  (models/map->Event (event-transformer resp-map)))
