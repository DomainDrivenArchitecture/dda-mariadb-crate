(defproject org.domaindrivenarchitecture/dda-mysql-crate "0.1.1-SNAPSHOT"
  :description "Iptables crate from the DomainDrivenArchitecture pallet project"
  :url "https://www.domaindrivenarchitecture.org"
  :license {:name "Apache License, Version 2.0"
             :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[prismatic/schema "1.0.5"]
                 [metosin/schema-tools "0.7.0"]
                 [org.clojure/clojure "1.7.0"]
                 [com.palletops/pallet "0.8.11"]]
  :repositories [["snapshots" :clojars]
                 ["releases" :clojars]]
  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]]
  :pallet {:source-paths ["src"]}
  :profiles {:dev
             {:dependencies
              [[com.palletops/pallet "0.8.11" :classifier "tests"]
               ]
              :plugins
              [[com.palletops/pallet-lein "0.8.0-alpha.1"]]}
              :leiningen/reply
               {:dependencies [[org.slf4j/jcl-over-slf4j "1.7.2"]]
                :exclusions [commons-logging]}}
  :local-repo-classpath true
  :classifiers {:tests {:source-paths ^:replace ["test"]
                        :resource-paths ^:replace []}})


