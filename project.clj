(defproject datis "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.12.5"]
                 [org.clojure/data.json "2.5.2"]
                 [com.fasterxml.jackson.core/jackson-databind "2.22.1"]
                 [duct/core "0.8.1"]
                 [duct/module.ataraxy "0.3.0"]
                 [duct/module.logging "0.5.0"]
                 [duct/module.sql "0.6.1"]
                 [duct/module.web "0.7.4"]
                 [hden/cloud-pubsub-batch-publisher "1.1.2-SNAPSHOT"]
                 [hden/debezium-embedded "4.0.0-SNAPSHOT"
                  :exclusions [org.eclipse.jetty.ee10/jetty-ee10-servlet
                               org.eclipse.jetty.ee10/jetty-ee10-servlets
                               org.eclipse.jetty/jetty-client]]
                 [io.debezium/debezium-connector-postgres "3.6.1.Final"]
                 [org.xerial/sqlite-jdbc "3.53.2.1"]]
  :plugins [[duct/lein-duct "0.12.3"]
            [lein-cloverage "1.2.4"]]
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
                  :dependencies   [[integrant/repl "0.5.1"]
                                   [camel-snake-kebab "0.4.3"]
                                   ;;  [org.slf4j/slf4j-simple "2.0.18"]
                                   [hawk "0.2.11"]
                                   [eftest "0.6.0"]
                                   [kerodon "0.9.1"]]}})
