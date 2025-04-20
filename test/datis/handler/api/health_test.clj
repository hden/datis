(ns datis.handler.api.health-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [ring.mock.request :as mock]
            [datis.service.debezium.core :refer [start! stop! Engine]]
            [datis.handler.api.health]))

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
    (let [spy (atom false)
          engine (TestEngine. spy)
          handler (ig/init-key :datis.handler.api/health {:engine engine})]
      (is (= [:ataraxy.response/internal-server-error {:running false}]
             (handler (mock/request :get "/health"))))
      (start! engine)
      (is (= [:ataraxy.response/ok {:running true}]
             (handler (mock/request :get "/health"))))
      (stop! engine)
      (is (= [:ataraxy.response/internal-server-error {:running false}]
             (handler (mock/request :get "/health")))))))
