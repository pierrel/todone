(ns todone.core
  (:require [projuctivity.core :as proj]))



(comment
  (proj/assign-users!)

  @proj/calendar-user



  (proj/auth-all)

  (proj/events-between "2021-01-01" "2021-03-01")


  (let [{:keys [clientid]} @proj/calendar-user]
    clientid)
  )