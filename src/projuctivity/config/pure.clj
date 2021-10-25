(ns projuctivity.config.pure
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

;; TODO probably move this somewhere more central. There are a bunch of places that should use the same spec or do make use of these.
(defn non-empty-string? [s]
  (and (string? s) (not= s "")))
(s/def :projuctivity.config/non-empty-string
  (s/with-gen non-empty-string?
    (fn []
      (gen/such-that #(and (non-empty-string? %)
                           (< 6 (.length %)))
                     (gen/string-alphanumeric)))))
(s/def :projuctivity.config/non-empty-ascii-string
  (s/with-gen non-empty-string?
    (fn []
      (gen/such-that #(and (non-empty-string? %)
                           (< 6 (.length %)))
                     (gen/string-ascii)))))
(s/def :projuctivity.config/clientid :projuctivity.config/non-empty-string)
(s/def :projuctivity.config/client-secret :projuctivity.config/non-empty-string)
(s/def :projuctivity.config/tenant :projuctivity.config/non-empty-string)
(s/def :projuctivity.config/service #{:calendar :tasks})
(s/def :projuctivity.config/services (s/coll-of :projuctivity.config/service
                                                :kind vector?
                                                :distinct true))
(s/def :projuctivity.config/keystorepass
  :projuctivity.config/non-empty-ascii-string)
(s/def :projuctivity.config/ssl-keystore
  (s/with-gen non-empty-string?
    (fn []
      (gen/fmap #(format "%s.jks" %)
                (gen/such-that #(not= % "")
                               (gen/string-alphanumeric))))))
(s/def :projuctivity.config/server (s/keys :req-un
                                           [:projuctivity.config/keystorepass
                                            :projuctivity.config/ssl-keystore]))
(s/def :projuctivity.config/msgraph
  (s/keys :req-un [:projuctivity.config/clientid
                   :projuctivity.config/tenant
                   :projuctivity.config/services]
          :opt-un [:projuctivity.config/client-secret]))
(s/def :projuctivity.config/service-config :projuctivity.config/msgraph)
(s/def :projuctivity.config/config
  (s/keys :req-un [:projuctivity.config/server
                   :projuctivity.config/msgraph]))

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
