; Licensed to the Apache Software Foundation (ASF) under one
; or more contributor license agreements. See the NOTICE file
; distributed with this work for additional information
; regarding copyright ownership. The ASF licenses this file
; to you under the Apache License, Version 2.0 (the
; "License"); you may not use this file except in compliance
; with the License. You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

; TODO: verify passwords < 16

(ns dda.pallet.dda-mariadb-crate.infra.maria-db
  (:require
    [schema.core :as s]
    [clojure.string :as string]
    [pallet.stevedore :as stevedore]
    [pallet.actions :as actions]))

(defn install-mariadb
  [root-password start-on-boot]
  (actions/debconf-set-selections {:package "mariadb-server"
                                   :question "mysql-server/root_password"
                                   :type "password" :value root-password})
  (actions/debconf-set-selections {:package "mariadb-server"
                                   :question "mysql-server/root_password_again"
                                   :type "password" :value root-password})
  (actions/debconf-set-selections {:package "mariadb-server"
                                   :question "mysql-server/start_on_boot"
                                   :type "boolean" :value start-on-boot})
  (actions/package "mariadb-server"))



(defn install-mysql-java-connector
  [& {:keys [connector-directory
             link-directory]}]
  (actions/remote-directory
    connector-directory
    :url "https://downloads.mariadb.com/Connectors/java/connector-java-2.0.3/mariadb-java-client-2.0.3.jar"
    :mode "660"
    :owner "root"
    :group "root"))
