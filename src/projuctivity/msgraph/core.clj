(ns projuctivity.msgraph.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [projuctivity.msgraph.utils :as utils]
            [projuctivity.msgraph.auth :as auth]))

(def scopes ["user.read"
             "Tasks.ReadWrite"
             "Calendars.ReadWrite"])

(def config (assoc (utils/load-edn ".config.edn")
                   :scopes scopes))

(def base-url "https://graph.microsoft.com/")

(defn get
  ([resource params token]
   (try
     (let [url (format "%s/v1.0/%s" base-url resource)
           params {:headers {:authorization (format "Bearer %s" token)}
                   :query-params params}
           resp (http/get url params)]
       (json/parse-string (:body resp)))
     (catch Exception e
       (if (= (:status (ex-data e)) 401)
         (do
           (auth/refresh-token config)
           (get resource params))
         (throw e)))))
  ([resource params]
   (let [token (auth/token config)]
     (get resource params token))))
