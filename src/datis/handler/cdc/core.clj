(ns datis.handler.cdc.core
  (:require [integrant.core :as ig]
            [datis.boundary.pubsub.core :as pubsub]))

(defmethod ig/init-key ::event-handler [_ {:keys [publisher]}]
  (fn [events]
    (pubsub/publish! publisher {:events (map :value events)})))
