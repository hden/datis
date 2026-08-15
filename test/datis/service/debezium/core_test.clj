(ns datis.service.debezium.core-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [datis.service.debezium.core :as debezium]
   [debezium-embedded.core :as embedded]
   [integrant.core :as ig])
  (:import
   (java.sql DriverManager)
   (java.util.concurrent Executors)))

(defn- seed-inventory! []
  (with-open [connection (DriverManager/getConnection "jdbc:postgresql://localhost:5432/postgres"
                                                      "postgres"
                                                      "postgres")
              statement (.createStatement connection)]
    (.execute statement "CREATE SCHEMA IF NOT EXISTS inventory")
    (.execute statement "CREATE TABLE IF NOT EXISTS inventory.customers (id INTEGER PRIMARY KEY, first_name TEXT NOT NULL, last_name TEXT NOT NULL, email TEXT NOT NULL)")
    (.execute statement "INSERT INTO inventory.customers (id, first_name, last_name, email) VALUES (9999, 'CI', 'Fixture', 'ci@datis.test') ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email")))

(deftest stop-delegates-to-the-embedded-engine-before-polling
  (let [stops    (atom [])
        executor (Executors/newSingleThreadExecutor)
        boundary (datis.service.debezium.core.Boundary. ::engine executor nil nil)]
    (try
      (with-redefs [embedded/polling? (constantly false)
                    embedded/stop! (fn [engine options]
                                     (swap! stops conj [engine options])
                                     nil)]
        (debezium/stop! boundary)
        (is (= [[::engine {:timeout-ms 5000}]] @stops)))
      (finally
        (.shutdownNow executor)))))

(deftest debezium-core-test
  (testing "init / halt"
    (seed-inventory!)
    (let [events (promise)
          arg-map {:config {:schema.include.list "inventory"}
                   :handler (fn [records]
                              (deliver events records))}
          engine (ig/init-key :datis.service.debezium/engine arg-map)]
      (try
        (let [records (deref events 10000 ::timed-out)]
          (is engine)
          (is (= {:running true} (debezium/status engine)))
          (is (vector? records))
          (when (vector? records)
            (is (every? #(= #{:offset :value} (set (keys %)))
                        records))
            (is (some #(= "ci@datis.test" (get-in % [:value :after :email]))
                      records))))
        (finally
          (ig/halt-key! :datis.service.debezium/engine engine))))))
