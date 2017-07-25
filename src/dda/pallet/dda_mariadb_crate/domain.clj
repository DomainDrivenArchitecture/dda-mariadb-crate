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

(ns dda.pallet.dda-mariadb-crate.domain
  (:require
    [schema.core :as s]
    [dda.pallet.dda-mariadb-crate.infra :as infra]))

(def DomainConfig {})

(def JavaConnector {:connector-directory "/var/lib/maria-db"
                    :download-url "https://downloads.mariadb.com/Connectors/java/connector-java-2.0.3/mariadb-java-client-2.0.3.jar"})

(s/defn ^:always-validate infra-configuration :- infra/AppConfigElement
  [domain-config :- DomainConfig]
  (let [{:keys []} domain-config]
    {infra/facility
      {:root-passwd "test1234"
       :settings #{}
       :db-type :maria
       :db [{:db-name "test-db"
             :db-user-name "db-user"
             :db-user-passwd "secret"}]}}))
