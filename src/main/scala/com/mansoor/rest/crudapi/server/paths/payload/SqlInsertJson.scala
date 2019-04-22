package com.mansoor.rest.crudapi.server.paths.payload

case class SqlInsertJson(schema: String,
                         table: String,
                         rows: List[Map[String, Any]],
                         pkCols: Option[List[String]])