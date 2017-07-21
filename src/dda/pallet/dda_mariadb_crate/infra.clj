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

(ns dda.pallet.dda-mariadb-crate.infra
  (:require
    [schema.core :as s]
    [clojure.string :as string]
    [pallet.stevedore :as stevedore]
    [pallet.actions :as actions]
    [pallet.crate :as crate]
    [dda.pallet.core.dda-crate :as dda-crate]))

(def facility :dda-mariadb)
(def version  [0 2 0])

(def DbConfig
  "Represents the database configuration."
  {:root-passwd s/Str
   :db-name s/Str
   :user-name s/Str
   :user-passwd s/Str})

(def AppConfigElement
  {facility DbConfig})

(defn install-mariadb
  [& {:keys [db-root-password
             start-on-boot]
      :or {db-root-password "test1234"
           start-on-boot true}}]
  (actions/debconf-set-selections {:package "mariadb-server"
                                   :question "mariadb-server/root_password"
                                   :type "password" :value db-root-password})
  (actions/debconf-set-selections {:package "mariadb-server"
                                   :question "mariadb-server/root_password_again"
                                   :type "password" :value db-root-password})
  (actions/debconf-set-selections {:package "mariadb-server"
                                   :question "mariadb-server/start_on_boot"
                                   :type "boolean" :value start-on-boot})
  (actions/package "mariadb-server"))



(defn install-mysql-java-connector
  [& {:keys [connector-directory
             link-directory]}]
  (actions/remote-directory
    connector-directory
    :url "http://cdn.mysql.com/Downloads/Connector-J/mysql-connector-java-5.1.38.tar.gz"
    :unpack :tar
    :recursive true
    :mode "660"
    :owner "root"
    :group "root")
  (actions/symbolic-link
    (str connector-directory "/mysql-connector-java-5.1.38/mysql-connector-java-5.1.38-bin.jar")
    (str link-directory "/mysql-connector-java-5.1.38-bin.jar")
    :action :create))

(s/defmethod dda-crate/dda-configure facility
  [dda-crate config]
  "dda-mariadb: configure")

(s/defmethod dda-crate/dda-install facility
  [dda-crate config]
  "dda-mariadb: install routine")

(def dda-mariadb-crate
  (dda-crate/make-dda-crate
   :facility facility
   :version version))

(def with-mariadb
  (dda-crate/create-server-spec dda-mariadb-crate))
