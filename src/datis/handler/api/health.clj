(ns datis.handler.api.health
  (:require
   [datis.service.debezium.core :refer [status]]
   [integrant.core :as ig]))

(defmethod ig/init-key :datis.handler.api/health [_ {:keys [engine]}]
  (fn [_request]
    (let [engine-status (status engine)]
      {:status (if (:running engine-status) 200 500)
       :body   engine-status})))
