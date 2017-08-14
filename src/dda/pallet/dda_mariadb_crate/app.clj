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

(ns dda.pallet.dda-mariadb-crate.app
  (:require
    [schema.core :as s]
    [dda.cm.group :as group]
    [dda.pallet.dda-config-crate.infra :as config-crate]
    [dda.pallet.dda-mariadb-crate.infra :as infra]
    [dda.pallet.dda-mariadb-crate.domain :as domain]))

(def InfraResult infra/InfraResult)

(def AppConfig
  {:group-specific-config
   {s/Keyword InfraResult}})

(s/defn ^:allways-validate create-app-configuration :- AppConfig
  [config :- infra/ServerConfig
   group-key :- s/Keyword]
  {:group-specific-config
     {group-key config}})

(def with-mariadb infra/with-mariadb)

(defn app-configuration
 [domain-config & {:keys [group-key] :or {group-key :dda-mariadb-group}}]
 (s/validate domain/DomainConfig domain-config)
 (create-app-configuration (domain/infra-configuration domain-config)
                           group-key))

(s/defn ^:always-validate mariadb-group-spec
 [app-config :- AppConfig]
 (group/group-spec
   app-config [(config-crate/with-config app-config)
               with-mariadb]))
