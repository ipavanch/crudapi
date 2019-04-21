package com.mansoor.rest.crudapi.server.paths.payload

case class SqlDeleteJson(schema: String,
                         table: String,
                         where: String)
