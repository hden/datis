(ns datis.handler.api.health
  (:require [integrant.core :as ig]
            [datis.service.debezium.core :refer [status]]
            [ataraxy.response :as response]))

(defmethod ig/init-key :datis.handler.api/health [_ {:keys [engine]}]
  (fn [{[_] :ataraxy/result}]
    (let [status (status engine)]
      (if (:running status)
        [::response/ok status]
        [::response/internal-server-error status]))))
