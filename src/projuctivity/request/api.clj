(ns projuctivity.request.api)

(defprotocol HTTPRequest
  "Makes HTTP requests"
  (get [model resource params] "Makes a GET request")
  (post [model resource params] "Makes a POST request"))
