(ns datis.boundary.pubsub.core-test
  (:require [clojure.test :refer [deftest is use-fixtures testing]]
            [cloud-pubsub-batch-publisher.core :refer [publisher]]
            [cloud-pubsub-batch-publisher.emulator :as emulator]
            [datis.boundary.pubsub.core :as pubsub]))

(def ^:const topic "projects/test-project/topics/test-topic")
(def context (emulator/context {}))

(use-fixtures :once (emulator/create-fixture topic context))

(deftest pubsub-test
  (testing "publish events"
    (let [publisher (publisher topic context)
          events [{:op :insert
                   :source {:name "test-source"}
                   :after {:id 1 :name "test"}}
                  {:op :update
                   :source {:name "test-source"}
                   :after {:id 2 :name "test2"}}]]
      (pubsub/publish! publisher events)
      (is :ok))))
