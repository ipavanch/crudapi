package com.mansoor.rest.crudapi.server.paths.payload

case class SqlUpdateJson(schema: String,
                         table: String,
                         set: Map[String, String],
                         where: String)
