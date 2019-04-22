package com.mansoor.rest.crudapi.utils.db

import doobie.util.query.Query0
import doobie.util.update.Update0

trait RowOps {
  def selectRows(schema: String, table: String, cols: List[String], where: Option[String], limit: Option[Int]): Query0[String]
  def insertRow(schema: String, table: String, row: Map[String, Any]): Update0
  def updateRow(schema: String, table: String, set: Map[String, Any], where: String): Update0
  def deleteRow(schema: String, table: String, where: String): Update0
}
