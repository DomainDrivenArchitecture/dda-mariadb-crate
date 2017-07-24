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
    [dda.pallet.core.dda-crate :as dda-crate]
    [dda.pallet.dda-mariadb-crate.infra.maria-db :as maria]))

(def facility :dda-mariadb)
(def version  [0 2 0])

(def DbConfig
  "Represents the database configuration."
  {:db-name s/Str
   :db-user-name s/Str
   :db-user-passwd s/Str})

(def ServerConfig
  "Represents the database configuration."
  {:root-passwd s/Str
   (s/optional-key :start-on-boot) s/Bool
   :db [DbConfig]})

(def AppConfigElement
  {facility ServerConfig})

(defn init
  "init package management"
  []
  (actions/package-manager :update))

(s/defmethod dda-crate/dda-init facility
  [dda-crate config]
  "dda mariadb: init routine"
    (init))

(s/defmethod dda-crate/dda-configure facility
  [dda-crate config]
  "dda-mariadb: configure")

(s/defmethod dda-crate/dda-install facility
  [dda-crate config]
  "dda-mariadb: install routine"
  (let [{:keys [root-passwd start-on-boot] :or {start-on-boot true}} config]
    (maria/install-mariadb root-passwd start-on-boot)))

(def dda-mariadb-crate
  (dda-crate/make-dda-crate
   :facility facility
   :version version))

(def with-mariadb
  (dda-crate/create-server-spec dda-mariadb-crate))
