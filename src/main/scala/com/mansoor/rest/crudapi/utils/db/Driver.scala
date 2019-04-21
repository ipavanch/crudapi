package com.mansoor.rest.crudapi.utils.db

import com.mansoor.rest.crudapi.utils.db.DBType.DBType

object Driver {
  def get(dbType: DBType): String = dbType match {
    case DBType.postgres => "org.postgresql.Driver"
  }
}
