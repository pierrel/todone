(ns projuctivity.request.api)

(defprotocol HTTPRequest
  "Makes HTTP requests"
  (get [model resource params] "Makse a GET request"))