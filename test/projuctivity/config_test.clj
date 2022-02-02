(ns projuctivity.config-test
  (:use [helper])
  (:require [projuctivity.config :as sut]
            [projuctivity.config.pure :as pure]
            [helper :as helper]
            [clojure.test :as t]
            [clojure.spec.test.alpha :as spectest]))

(def example-config
  {:msgraph {:clientid "comething"
             :tenant "consumers"
             :services [:calendar :tasks]}
   :server {:keystorepass "apass"
            :ssl-keystore "keystore.jks"}})

(t/deftest spec-check
  (helper/check-and-test-specs
   'projuctivity.config
   :to-stub #{`projuctivity.config/load-config}))

(t/deftest service-part
  (t/is (= [:msgraph (:msgraph example-config)]
           (pure/service-part :tasks example-config))))
