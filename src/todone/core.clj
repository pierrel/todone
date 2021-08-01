(ns todone.core
  (:require [projuctivity.core :as proj]))



(proj/assign-users!)
@proj/calendar-user
;; (projuctivity.msgraph.core/auth @proj/calendar-user)
