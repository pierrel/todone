(ns projuctivity.config-test
  (:use [helper])
  (:require [projuctivity.config :as sut]
            [projuctivity.config.pure :as pure]
            [clojure.test :as t]
            [clojure.spec.test.alpha :as spectest]))

(def instrumented ['pure/service-part
                   'sut/server-config
                   'sut/calendar-config
                   'sut/tasks-config])

(spectest/instrument instrumented)

(def example-config
  {:msgraph {:clientid "comething"
             :tenant "consumers"
             :services [:calendar :tasks]}
   :server {:keystorepass "apass"
            :ssl-keystore "keystore.jks"}})

(t/deftest spec-check
  (let [results (spectest/summarize-results
                 (spectest/check instrumented))]))

(t/deftest service-part
  (t/is (= [:msgraph (:msgraph example-config)]
           (pure/service-part :tasks example-config))))
