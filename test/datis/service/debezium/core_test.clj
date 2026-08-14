(ns datis.service.debezium.core-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [datis.service.debezium.core :as debezium]
   [integrant.core :as ig]))

(deftest debezium-core-test
  (testing "init / halt"
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
                        records))))
        (finally
          (ig/halt-key! :datis.service.debezium/engine engine))))))
