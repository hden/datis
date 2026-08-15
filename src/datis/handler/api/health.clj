(ns datis.handler.api.health
  (:require
   [ataraxy.response :as response]
   [datis.service.debezium.core :refer [status]]
   [integrant.core :as ig]))

(defmethod ig/init-key :datis.handler.api/health [_ {:keys [engine]}]
  (fn [{[_] :ataraxy/result}]
    (let [status (status engine)]
      (if (:running status)
        [::response/ok status]
        [::response/internal-server-error status]))))
