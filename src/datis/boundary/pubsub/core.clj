(ns datis.boundary.pubsub.core
  (:require [integrant.core :as ig]
            [clojure.data.json :as json]
            [cloud-pubsub-batch-publisher.core :as pubsub]))

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

(defn publish! [publisher {:keys [events]}]
  (pubsub/publish! publisher (map event->message events)))

(defmethod ig/init-key ::publisher [_ {:keys [topic]}]
  (pubsub/publisher topic))

(defmethod ig/halt-key! ::publisher [_ publisher]
  (pubsub/shutdown! publisher))
