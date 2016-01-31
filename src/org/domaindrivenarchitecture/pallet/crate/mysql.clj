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

(ns org.domaindrivenarchitecture.pallet.crate.mysql
  (:require
    [clojure.string :as string]
    [pallet.stevedore :as stevedore]
    [pallet.actions :as actions]
    [pallet.api :as api]
    [pallet.crate :as crate]
))

(defn install-mysql
  [& {:keys [db-root-password 
             start-on-boot]
      :or {db-root-password "test1234"
           start-on-boot true}}]
  (actions/debconf-set-selections {:package "mysql-server" 
                                   :question "mysql-server/root_password" 
                                   :type "password" :value db-root-password})
  (actions/debconf-set-selections {:package "mysql-server" 
                                   :question "mysql-server/root_password_again" 
                                   :type "password" :value db-root-password})
  (actions/debconf-set-selections {:package "mysql-server" 
                                   :question "mysql-server/start_on_boot" 
                                   :type "boolean" :value start-on-boot})
  (actions/package "mysql-server")
)


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
    :action :create)
  )


(defn- mysql-script*
  [db-user-name db-passwd sql-script]
  (stevedore/script
   ("{\n" mysql "-u" ~db-user-name ~(str "--password=" db-passwd)
    ~(str "<<EOF\n" (string/replace sql-script "`" "\\`") "\nEOF\n}"))
   )
  )

(defn mysql-script
  "Execute a mysql script. If no user is given it defaults to root."
  [& {:keys [db-user-name 
             db-passwd
             sql-script]
      :or {db-user-name "root"
           db-passwd "test1234"}}]
  (actions/exec-checked-script
    "MYSQL command"
    ~(mysql-script* db-user-name db-passwd sql-script)
    )
  )

(defn create-database
  [& {:keys [db-user-name
             db-passwd
             db-name
             create-options]
      :or {db-user-name "root"
           db-passwd "test1234"
           create-options ""}}]
  (mysql-script
    :db-user-name db-user-name 
    :db-passwd db-passwd
    :sql-script (format "CREATE DATABASE IF NOT EXISTS `%s` %s" db-name create-options)
    )
  )

(defn create-user
  [& {:keys [db-root-user-name
             db-root-passwd
             db-user
             db-passwd]
      :or {db-root-user-name "root"
           db-root-passwd "test1234"}}]
    (mysql-script
      :db-user-name db-root-user-name 
      :db-passwd db-root-passwd
      :sql-script              
      (format "CREATE USER '%s'@'localhost' IDENTIFIED BY '%s'" db-user db-passwd)
    )
  )

(defn grant
  "examples for level are `database`.*. \n
users are defined at localhost."
  [& {:keys [db-root-user-name
             db-root-passwd
             db-user-name
             db-user-passwd
             grant-privileges
             grant-level]
      :or {db-root-user-name "root"
           db-root-passwd "test1234"
           grant-privileges "ALL"}}]
  (if db-user-passwd
    (mysql-script
      :db-user-name db-root-user-name 
      :db-passwd db-root-passwd 
      :sql-script       
      (format "GRANT %s ON %s TO '%s'@'localhost' identified by '%s'" 
              grant-privileges grant-level db-user-name db-user-passwd))
    (mysql-script
      :db-user-name db-root-user-name 
      :db-passwd db-root-passwd 
      :sql-script
      (format "GRANT %s ON %s TO %s" grant-privileges grant-level db-user-name))
    )
  )