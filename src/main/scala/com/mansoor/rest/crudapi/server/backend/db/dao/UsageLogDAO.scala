package com.mansoor.rest.crudapi.server.backend.db.dao

import com.mansoor.rest.crudapi.server.backend.db.Tables
import com.mansoor.rest.crudapi.server.backend.db.dao.columns.{UsageLogColumns, VaultColumns}
import com.mansoor.rest.crudapi.server.backend.db.dto.UsageLogDTO
import com.mansoor.rest.crudapi.utils.db.CRUD
import doobie.util.fragment.Fragment
import doobie.util.update.Update0

class UsageLogDAO extends UsageLogColumns with CRUD {
  override def table: String = Tables.USAGE_LOG

  override def create(): Update0 = {
    val query: String =
      s"""
         |CREATE TABLE IF NOT EXISTS $table(
         |  $id        SERIAL UNIQUE,
         |  $vaultNs   VARCHAR(100),
         |  $vaultUser VARCHAR(50),
         |  $runMode   VARCHAR(20) NOT NULL,
         |  $operation VARCHAR(20) NOT NULL,
         |  $entity    VARCHAR(100) NOT NULL,
         |  $status    BOOLEAN NOT NULL,
         |  $msg       TEXT NOT NULL,
         |  $entryTs   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
         |  FOREIGN KEY ($vaultNs, $vaultUser) REFERENCES ${Tables.VAULT}(${VaultColumns.namespace}, ${VaultColumns.user}),
         |  PRIMARY KEY ($id)
         |);
     """.stripMargin

    println(query)

    Fragment.const(query).update
  }

  override def insert(record: Any): Update0 = {
    val rec: UsageLogDTO = record.asInstanceOf[UsageLogDTO]
    val query: String =
      s"""
         |INSERT INTO $table ($vaultNs, $vaultUser, $runMode, $operation, $entity, $status, $msg)
         |VALUES ('${rec.vaultNs.trim}', '${rec.vaultUsr.trim}', '${rec.runMode.trim}',
         |'${rec.operation.trim}', '${rec.entity.trim}', ${rec.status}, '${rec.msg.trim}'
         |);
       """.stripMargin

    Fragment.const(query).update
  }
}
