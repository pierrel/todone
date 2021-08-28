(ns todone.core
  (:require [projuctivity.core :as proj]))

(proj/assign-users!)
@proj/calendar-user


(proj/auth-all)

(let [{:keys [clientid]} @proj/calendar-user]
  clientid)