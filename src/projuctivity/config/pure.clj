(ns projuctivity.config.pure
  (:require [clojure.spec.alpha :as s]))

;; TODO probably move this somewhere more central
(s/def :projuctivity.config/clientid string?)
(s/def :projuctivity.config/tenant string?)
;; TODO update services spec to only match a set of services
(s/def :projuctivity.config/services (s/coll-of keyword?
                                                :kind vector?
                                                :distinct true))
(s/def :projuctivity.config/keystorepass string?)
(s/def :projuctivity.config/ssl-keystore string?)
(s/def :projuctivity.config/server (s/keys :req-un
                                           [:projuctivity.config/keystorepass
                                            :projuctivity.config/ssl-keystore]))
(s/def :projuctivity.config/msgraph (s/keys :req-un [:projuctivity.config/clientid
                                                     :projuectivity.config/tenant
                                                     :projuctivity.config/services]))
(s/def :projuctivity.config/service-config :projuctivity.config/msgraph)
(s/def :projuctivity.config/config (s/keys :req-un [:projuctivity.config/server]
                                           :opt-un [:projuctivity.config/msgraph]))

(s/fdef service-part
  :args (s/cat :part keyword?
               :config :projuctivity.config/config)
  :ret (s/tuple keyword? :projuctivity.config/config))
(defn service-part
  "Returns the specified service-specific `part` of the `config`
  including the service provider."
  [part config]
  (first (filter (fn [[_ key]]
                   (if-let [services (get key :services)]
                     (some #{part} services)))
                 config)))
