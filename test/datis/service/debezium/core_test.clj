(ns datis.service.debezium.core-test
  (:require [clojure.test :refer :all]
            [promesa.core :as promesa]
            [integrant.core :as ig]
            [datis.service.debezium.core :as debezium]))

(deftest debezium-core-test
  (testing "init / halt"
    (let [events (promesa/deferred)
          arg-map {:config {:schema.include.list "inventory"}
                   :handler (fn [records]
                              (promesa/resolve! events records))}
          engine (ig/init-key :datis.service.debezium/core arg-map)
          records (promesa/await events)]
      (is engine)
      (is (vector? records))
      (is (every? #(= #{:offset :value} (set (keys %)))
                  records)))))
