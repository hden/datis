(ns datis.service.debezium.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [promesa.core :as promesa]
            [integrant.core :as ig]
            [datis.service.debezium.core :as debezium]))

(deftest debezium-core-test
  (testing "init / halt"
    (let [events (promesa/deferred)
          arg-map {:config {:schema.include.list "inventory"}
                   :handler (fn [records]
                              (promesa/resolve! events records))}
          engine (ig/init-key :datis.service.debezium/engine arg-map)
          records (promesa/await events)]
      (is engine)
      (is (= {:running true} (debezium/status engine)))
      (is (vector? records))
      (is (every? #(= #{:offset :value} (set (keys %)))
                  records)))))
