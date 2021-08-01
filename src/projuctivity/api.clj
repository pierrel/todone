(ns projuctivity.api)

(defprotocol EventSearch
  "Search through and return `Events`."
  (events [user date1 date2] "A lazy list of events between date1 and date2"))

(defprotocol Auth
  "Authentication-related methods."
  (auth [user] "Gets all authorization done."))

(defprotocol TaskSearch
  "Search through and return task list"
  (open-tasks [user] "A lazy list of open tasks for the user"))
