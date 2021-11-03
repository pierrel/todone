(ns projuctivity.config.pure
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [projuctivity.spec :as lspec]))

;; TODO probably move this somewhere more central. There are a bunch of places that should use the same spec or do make use of these.
(s/def :projuctivity.config/clientid
  (s/with-gen lspec/non-empty-string?
    #(lspec/gen-char-len gen/char-ascii 1 10)))
(s/def :projuctivity.config/client-secret
  (s/with-gen lspec/non-empty-string?
    #(lspec/gen-char-len gen/char-ascii 1 10)))
(s/def :projuctivity.config/tenant
  (s/with-gen lspec/non-empty-string?
    #(lspec/gen-char-len gen/char-alpha 3 10)))
(s/def :projuctivity.config/service #{:calendar :tasks})
(s/def :projuctivity.config/services (s/coll-of :projuctivity.config/service
                                                :kind vector?
                                                :distinct true))
(s/def :projuctivity.config/keystorepass
  (s/with-gen lspec/non-empty-string?
    #(lspec/gen-char-len gen/char-alpha 6 10)))
(s/def :projuctivity.config/ssl-keystore
  (s/with-gen lspec/non-empty-string?
    (fn []
      (gen/fmap #(format "%s.jks" %)
                (lspec/gen-char-len gen/char-alpha 4 10)))))
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
  :args (s/cat :part :projuctivity.config/service
               :config :projuctivity.config/config)
  :ret (s/nilable (s/tuple :projuctivity.config/service
                           :projuctivity.config/service-config)))
(defn service-part
  "Returns the specified service-specific `part` of the `config`
  including the service provider."
  [part config]
  (first (filter (fn [[_ key]]
                   (if-let [services (get key :services)]
                     (some #{part} services)))
                 config)))
