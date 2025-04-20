(ns datis.service.debezium.core
  (:require [debezium-embedded.core :as debezium]
            [duct.logger :refer [log]]
            [integrant.core :as ig])
  (:import [java.util.concurrent Executors TimeUnit]))

(def default-config
  {:name "datis"
   :connector.class "io.debezium.connector.postgresql.PostgresConnector"
   :database.hostname "localhost"
   :database.port "5432"
   :database.user "postgres"
   :database.password "postgres"
   :database.dbname "postgres"
   :topic.prefix "datis"
   :plugin.name "pgoutput"
   :offset.storage "org.apache.kafka.connect.storage.MemoryOffsetBackingStore"
   :offset.flush.interval.ms "0"
   :converter.schemas.enable "false"})

(defprotocol Engine
  (start! [this])
  (stop! [this])
  (status [this]))

(defrecord Boundary [engine executor logger]
  Engine
  (start! [this]
    (.execute executor engine)
    (when logger
      (log logger :info "Debezium engine started"))
    this)
  (stop! [_]
    (try
      (.shutdown executor)
      (while (not (.awaitTermination executor 5 TimeUnit/SECONDS))
        (when logger
          (log logger :info "Waiting another 5 seconds for the embedded engine to shut down...")))
      (catch InterruptedException e
        (when logger
          (log logger :error "Error stopping Debezium engine" e))
        (throw e))))
  (status [_]
    {:running (debezium/running? engine)}))

(defmethod ig/init-key :datis.service.debezium/engine [_ {:keys [config handler logger]
                                                          :or {handler identity}}]
  (let [executor (Executors/newSingleThreadExecutor)
        engine (->Boundary (debezium/create-engine {:config (merge default-config config)
                                                    :consumer handler})
                           executor
                           logger)]
    (start! engine)))

(defmethod ig/halt-key! :datis.service.debezium/engine [_ engine]
  (stop! engine))
