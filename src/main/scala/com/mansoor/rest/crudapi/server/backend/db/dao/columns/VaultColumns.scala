package com.mansoor.rest.crudapi.server.backend.db.dao.columns

case object VaultColumns extends VaultColumns

trait VaultColumns {
  val id: String = "id"
  val namespace: String = "namespace"
  val user: String = "usr"
  val dbType: String = "db_typ"
  val jdbcURL: String = "jdbc_url"
  val jdbcUser: String = "jdbc_usr"
  val jdbcPass: String = "jdbc_pass"
  val entryTs: String = "entry_ts"
}