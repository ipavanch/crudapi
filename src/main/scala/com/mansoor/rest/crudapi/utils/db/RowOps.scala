package com.mansoor.rest.crudapi.utils.db

import doobie.util.update.Update0

trait RowOps {
  def insertRow(schema: String, table: String, row: Map[String, String]): Update0
  def updateRow(schema: String, table: String, set: Map[String, String], where: String): Update0
  def deleteRow(schema: String, table: String, where: String): Update0
}
