(ns datis.handler.cdc.core-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [datis.boundary.pubsub.core :as pubsub]
            [datis.handler.cdc.core :as cdc]))

(defn mock-publisher [spy]
  (reify pubsub/Publisher
    (publish! [_ arg-map]
      (reset! spy arg-map))))

(deftest cdc-handler-test
  (testing "cdc handler"
    (let [spy (atom [])
          arg-map {:publisher (mock-publisher spy)}
          handler (ig/init-key :datis.handler.cdc.core/event-handler arg-map)
          data [{:value {:id 1 :name "test"}}
                {:value {:id 2 :name "test2"}}]]
      (is (fn? handler))
      (handler data)
      (is (= {:events (map :value data)}
             @spy)))))
