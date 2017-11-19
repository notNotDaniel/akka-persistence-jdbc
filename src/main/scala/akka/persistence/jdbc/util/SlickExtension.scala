/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package akka.persistence.jdbc.util

import akka.persistence.jdbc.config.SlickConfiguration
import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config
import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcBackend.Database


object SlickExtension extends ExtensionId[SlickExtensionImpl] with ExtensionIdProvider {
  override def lookup: SlickExtension.type = SlickExtension
  override def createExtension(system: ExtendedActorSystem) = new SlickExtensionImpl(system)
}

class SlickExtensionImpl(system: ExtendedActorSystem) extends Extension {
  private val dbProvider: SlickDatabaseProvider = // TODO lookup using fqcn
    new DefaultStickDatabaseProvider(system)

  def journalDatabase: Database = dbProvider.journalDatabase
  def readJournalDatabase: Database = dbProvider.readJournalDatabase
  def snapshotDatabase: Database = dbProvider.snapshotDatabase
  def journalProfile: JdbcProfile = dbProvider.journalProfile
  def readJournalProfile: JdbcProfile = dbProvider.readJournalProfile
  def snapshotProfile: JdbcProfile = dbProvider.snapshotProfile
}


trait SlickDatabaseProvider {
  def journalDatabase: Database
  def readJournalDatabase: Database
  def snapshotDatabase: Database
  def journalProfile: JdbcProfile
  def readJournalProfile: JdbcProfile
  def snapshotProfile: JdbcProfile
}

class DefaultStickDatabaseProvider(system: ActorSystem) extends SlickDatabaseProvider {
  val journalCfg: Config = system.settings.config.getConfig("jdbc-journal")
  val readJournalCfg: Config = system.settings.config.getConfig("jdbc-read-journal")
  val snapshotCfg: Config = system.settings.config.getConfig("jdbc-snapshot-store")

  lazy val journalDatabase: Database = SlickDatabase.forConfig(journalCfg, new SlickConfiguration(journalCfg))
  lazy val readJournalDatabase: Database = SlickDatabase.forConfig(readJournalCfg, new SlickConfiguration(readJournalCfg))
  lazy val snapshotDatabase: Database = SlickDatabase.forConfig(snapshotCfg, new SlickConfiguration(snapshotCfg))
  val journalProfile: JdbcProfile = SlickDriver.forDriverName(journalCfg)
  val readJournalProfile: JdbcProfile = SlickDriver.forDriverName(readJournalCfg)
  val snapshotProfile: JdbcProfile = SlickDriver.forDriverName(snapshotCfg)
}
