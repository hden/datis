(ns datis.service.debezium.core
  (:require
   [debezium-embedded.core :as debezium]
   [duct.logger :refer [log]]
   [integrant.core :as ig])
  (:import
   (java.util.concurrent Callable ExecutorService Executors Future ThreadFactory)))

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

(defn- daemon-thread-factory []
  (let [thread-number (atom 0)]
    (reify ThreadFactory
      (newThread [_ runnable]
        (doto (Thread. runnable (str "datis-debezium-" (swap! thread-number inc)))
          (.setDaemon true))))))

(defn- launch! [engine executor logger]
  (.submit ^ExecutorService executor
           ^Callable
           (reify Callable
             (call [_]
               (if-let [anomaly (debezium/start! engine {:executor executor})]
                 (when logger
                   (log logger :error "Debezium engine did not start:" anomaly))
                 (when logger
                   (log logger :info "Debezium engine started")))))))

(defrecord Boundary [engine executor start-task logger]
  Engine
  (start! [this]
    (assoc this :start-task (launch! engine executor logger)))
  (stop! [_]
    (try
      (when-let [anomaly (debezium/stop! engine {:timeout-ms 5000})]
        (throw (ex-info "Unable to stop Debezium engine" anomaly)))
      (finally
        (when start-task
          (.cancel ^Future start-task true))
        (.shutdownNow ^ExecutorService executor))))
  (status [_]
    {:running (debezium/polling? engine)}))

(defmethod ig/init-key :datis.service.debezium/engine [_ {:keys [config handler logger]
                                                          :or {handler identity}}]
  (let [executor (Executors/newFixedThreadPool 2 (daemon-thread-factory))
        engine (->Boundary (debezium/create-engine {::debezium/config (merge default-config config)
                                                    ::debezium/consumer handler
                                                    ::debezium/on-event
                                                    (fn [event]
                                                      (tap> event)
                                                      (when logger
                                                        (log logger :info "Debezium event:" event)))
                                                    ::debezium/default-shutdown-timeout-ms 5000})
                           executor
                           nil
                           logger)]
    (start! engine)))

(defmethod ig/halt-key! :datis.service.debezium/engine [_ engine]
  (stop! engine))
