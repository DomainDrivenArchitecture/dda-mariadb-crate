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
    [pallet.actions :as actions]
    [dda.pallet.core.infra :as core-infra]
    [dda.pallet.dda-mariadb-crate.infra.mysql-db :as mysql]
    [dda.pallet.dda-mariadb-crate.infra.maria-db :as maria]
    [dda.pallet.dda-mariadb-crate.infra.script :as script]))

(def facility :dda-mariadb)

(def DbConfig
  "Represents the database configuration."
  {:db-name s/Str
   :db-user-name s/Str
   :db-user-passwd s/Str
   (s/optional-key :create-options) s/Str})

(def ServerConfig
  "Represents the database configuration."
  {:root-passwd s/Str
   :db-type (s/enum :maria :mysql)
   :settings (hash-set (s/enum :start-on-boot))
   (s/optional-key :db) [DbConfig]
   (s/optional-key :user) []
   (s/optional-key :java-connector) {:connector-directory s/Str
                                     :download-url s/Str}})

(def InfraResult
  {facility ServerConfig})

(defn init
  "init package management"
  []
  (actions/package-manager :update))

(s/defmethod core-infra/dda-init facility
  [core-infra config]
  "dda mariadb: init routine"
  (init))

(s/defmethod core-infra/dda-install facility
  [core-infra config]
  "dda-mariadb: install routine"
  (let [{:keys [root-passwd java-connector settings db db-type]} config
        {:keys [start-on-boot]
         :or {start-on-boot true}} settings]
    (if (= :mysql db-type)
      (mysql/install-mysqldb root-passwd start-on-boot)
      (maria/install-mariadb root-passwd start-on-boot))
    (when (contains? config :java-connector)
      (let [{:keys [connector-directory download-url]} java-connector]
        (maria/install-java-connector connector-directory download-url)))
    (when (contains? config :db)
      (doseq [db-config db]
        (let [{:keys [db-name db-user-name db-user-passwd create-options]
               :or {create-options ""}} db-config]
          (script/init-database
           "root" root-passwd db-name db-user-name db-user-passwd create-options))))))

(s/defmethod core-infra/dda-configure facility
  [core-infra config]
  "dda-mariadb: configure")

(def dda-mariadb-crate
  (core-infra/make-dda-crate-infra
   :facility facility
   :infra-schema ServerConfig))

(def with-mariadb
  (core-infra/create-infra-plan dda-mariadb-crate))
