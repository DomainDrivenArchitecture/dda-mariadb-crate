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
    [dda.pallet.commons.secret :as secret]
    [dda.pallet.dda-mariadb-crate.infra :as infra]))

(def DbConfig
  "Represents the database configuration."
  {:db-name s/Str
   :db-user-name s/Str
   :db-user-passwd secret/Secret})

(def DomainConfig
  "Represents the database configuration."
  {:root-passwd secret/Secret
   :settings (hash-set (s/enum :with-java-connector))
   :db [DbConfig]})

(def DomainConfigResolved
  (secret/create-resolved-schema DomainConfig))

;TODO: make java connector operable
(def JavaConnector {:connector-directory "/var/lib/maria-db"
                    :download-url "https://downloads.mariadb.com/Connectors/java/connector-java-2.0.3/mariadb-java-client-2.0.3.jar"})

(s/defn ^:always-validate
  infra-configuration :- infra/InfraResult
  [domain-config :- DomainConfigResolved]
  (let [{:keys [root-passwd db]} domain-config]
    {infra/facility
      {:root-passwd root-passwd
       :settings #{:start-on-boot}
       :db-type :maria
       :db db}}))
