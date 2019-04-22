package com.mansoor.rest.crudapi.server.paths.payload

case class SqlSelectJson(schema: String,
                         table: String,
                         cols: List[String],
                         where: Option[String]
                        )
