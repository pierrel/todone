(ns projuctivity.models)

(defrecord Event
    [id
     subject
     body
     link
     start
     end
     organizer])

(defrecord Task
    [id
     status ;;
     title
     body
     due])

(defrecord User
    [token retry-token client-id secret tenant service])
