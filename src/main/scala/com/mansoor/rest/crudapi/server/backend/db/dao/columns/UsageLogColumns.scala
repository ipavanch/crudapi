package com.mansoor.rest.crudapi.server.backend.db.dao.columns

case object UsageLogColumns extends UsageLogColumns

trait UsageLogColumns {
  val id: String = "id"
  val vaultNs: String = "v_namespace"
  val vaultUser: String = "v_usr"
  val runMode: String = "run_mode"
  val operation: String = "operation"
  val entity: String = "entity"
  val status: String = "status"
  val msg: String = "msg"
  val entryTs: String = "entry_ts"
}
