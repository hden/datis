(ns datis.handler.cdc.core-test
  {:clj-kondo/config '{:linters {:unused-namespace {:level :off}}}}
  (:require [clojure.test :refer [deftest is testing]]
            [integrant.core :as ig]
            [cloud-pubsub-batch-publisher.core :as pubsub]
            [datis.handler.cdc.core :as cdc]))

(defn mock-publisher [spy]
  (reify pubsub/PublisherImpls
    (publish-impl! [_ arg-map]
      (reset! spy arg-map))))

(deftest cdc-handler-test
  (testing "cdc handler"
    (let [spy (atom [])
          arg-map {:publisher (mock-publisher spy)}
          handler (ig/init-key :datis.handler.cdc.core/event-handler arg-map)
          data [{:value {:op :insert
                         :source {:name "test-source"}
                         :after {:id 1 :name "test"}}}
                {:value {:op :update
                         :source {:name "test-source"}
                         :after {:id 2 :name "test2"}}}]]
      (is (fn? handler))
      (handler data)
      (is (= {:messages
              [{:message "{\"op\":\"insert\",\"source\":{\"name\":\"test-source\"},\"after\":{\"id\":1,\"name\":\"test\"}}"
                :metadata {"op" ":insert" "name" "test-source" "id" "1"}
                :ordering-key "test-source"}
               {:message "{\"op\":\"update\",\"source\":{\"name\":\"test-source\"},\"after\":{\"id\":2,\"name\":\"test2\"}}"
                :metadata {"op" ":update" "name" "test-source" "id" "2"}
                :ordering-key "test-source"}]}
             @spy)))))
