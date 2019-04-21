package com.mansoor.rest.crudapi.server.paths.payload

case class SqlInsertJson(schema: String,
                         table: String,
                         rows: List[Map[String, String]],
                         pkCols: Option[List[String]])