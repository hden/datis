(ns datis.handler.cdc.core
  (:require
   [datis.boundary.pubsub.core :as pubsub]
   [integrant.core :as ig]))

(defmethod ig/init-key ::event-handler [_ {:keys [publisher logger]}]
  (fn [events]
    (pubsub/publish! publisher {:events (map :value events)
                                :logger logger})))
