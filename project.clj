(defproject datis "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.12.1"]
                 [org.clojure/data.json "2.5.1"]
                 [com.fasterxml.jackson.core/jackson-databind "2.16.2"]
                 [duct/core "0.8.0"]
                 [duct/module.ataraxy "0.3.0"]
                 [duct/module.logging "0.5.0"]
                 [duct/module.sql "0.6.1"]
                 [duct/module.web "0.7.3"]
                 [hden/cloud-pubsub-batch-publisher "1.1.2-SNAPSHOT"]
                 [hden/debezium-embedded "3.1.0-SNAPSHOT"]
                 [io.debezium/debezium-connector-postgres "3.1.3.Final"]
                 [org.xerial/sqlite-jdbc "3.50.1.0"]]
  :plugins [[duct/lein-duct "0.12.3"]]
  :main ^:skip-aot datis.main
  :resource-paths ["resources" "target/resources"]
  :prep-tasks     ["javac" "compile" ["run" ":duct/compiler"]]
  :middleware     [lein-duct.plugin/middleware]
  :profiles
  {:dev  [:project/dev :profiles/dev]
   :repl {:prep-tasks   ^:replace ["javac" "compile"]
          :repl-options {:init-ns user}}
   :uberjar {:aot :all}
   :profiles/dev {}
   :project/dev  {:source-paths   ["dev/src"]
                  :resource-paths ["dev/resources"]
                  ;; :global-vars {*warn-on-reflection* true}
                  :dependencies   [[integrant/repl "0.3.2"]
                                   [funcool/promesa "11.0.678"]
                                   [camel-snake-kebab "0.4.3"]
                                  ;;  [org.slf4j/slf4j-simple "2.0.17"]
                                   [hawk "0.2.11"]
                                   [eftest "0.5.9"]
                                   [kerodon "0.9.1"]]}})
