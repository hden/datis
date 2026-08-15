(ns datis.handler.api.health-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [datis.handler.api.health]
   [datis.service.debezium.core :refer [Engine start! stop!]]
   [integrant.core :as ig]
   [ring.mock.request :as mock]))

(defrecord TestEngine [running]
  Engine
  (start! [this]
    (reset! running true)
    this)
  (stop! [this]
    (reset! running false)
    this)
  (status [_]
    {:running @running}))

(deftest health-test
  (testing "healthcheck endpoint"
    (let [running (atom false)
          engine (TestEngine. running)
          handler (ig/init-key :datis.handler.api/health {:engine engine})]
      (is (= {:status 500
              :body   {:running false}}
             (handler (mock/request :get "/health"))))
      (start! engine)
      (is (= {:status 200
              :body   {:running true}}
             (handler (mock/request :get "/health"))))
      (stop! engine)
      (is (= {:status 500
              :body   {:running false}}
             (handler (mock/request :get "/health")))))))
