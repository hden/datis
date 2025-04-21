(ns datis.boundary.pubsub.core
  (:require [integrant.core :as ig]
            [clojure.data.json :as json]
            [duct.logger :refer [log]]
            [cloud-pubsub-batch-publisher.core :as pubsub]
            [cloud-pubsub-batch-publisher.emulator :as emulator]))

(defn- event->metadata [event]
  (let [metadata (merge (select-keys event [:op])
                        (select-keys (:source event) [:connector :name :db :schema :table :snapshot :version])
                        (select-keys (:before event) [:id])
                        (select-keys (:after event) [:id]))]
    (into {} (map (fn [[k v]] [(name k) (str v)]) metadata))))

(defn- event->message [event]
  (let [s (json/write-str event)
        name (get-in event [:source :name])
        metadata (event->metadata event)]
    {:message s
     :metadata metadata
     :ordering-key name}))

(defn publish! [publisher {:keys [events logger]}]
  (when (seq events)
    (try
      (let [messages (map event->message events)
            published-message-ids (pubsub/publish! publisher {:messages messages})]
        (when logger
          (log logger :info (str "Published " (count events) " events"))
          (log logger :debug "Published messages:" {:message-ids published-message-ids})))
      (catch Exception e
        (when logger
          (log logger :error "Error publishing events:" e))
        (throw e)))))

(defmethod ig/init-key ::publisher [_ {:keys [topic logger]}]
  (if (System/getenv "PUBSUB_EMULATOR_HOST")
    (let [context (emulator/context {})]
      (when logger
        (log logger :debug "Creating topic:" topic))
      (emulator/create-topic! topic context)
      (pubsub/publisher topic context))
    (pubsub/publisher topic)))

(defmethod ig/halt-key! ::publisher [_ publisher]
  (pubsub/shutdown! publisher))
