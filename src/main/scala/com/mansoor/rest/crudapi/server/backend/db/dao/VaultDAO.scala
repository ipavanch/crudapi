package com.mansoor.rest.crudapi.server.backend.db.dao

import com.mansoor.rest.crudapi.server.backend.db.dao.columns.VaultColumns
import com.mansoor.rest.crudapi.utils.db.CRUD
import com.mansoor.rest.crudapi.log
import com.mansoor.rest.crudapi.server.backend.db.Tables
import doobie.util.fragment.Fragment
import doobie.util.update.Update0

class VaultDAO extends VaultColumns with CRUD {
  override def table: String = Tables.VAULT

  override def create(): Update0 = {
   val query: String =
     s"""
        |CREATE TABLE IF NOT EXISTS $table(
        |  $id        SERIAL UNIQUE,
        |  $namespace VARCHAR(100),
        |  $user      VARCHAR(50),
        |  $dbType    VARCHAR(50) NOT NULL,
        |  $jdbcURL   VARCHAR(200) NOT NULL,
        |  $jdbcUser  VARCHAR(100) NOT NULL,
        |  $jdbcPass  VARCHAR(100) NOT NULL,
        |  $entryTs   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        |  PRIMARY KEY ($namespace, $user)
        |);
     """.stripMargin

    log.info(query)

    Fragment.const(query).update
  }
}
