package com.mansoor.rest.crudapi.server.backend.db.dto

import com.mansoor.rest.crudapi.utils.db.DBType.DBType

case class VaultDTO(namespace: String,
                    user: String,
                    dbType: DBType,
                    jdbcURL: String,
                    jdbcUser: String,
                    jdbcPass: String)
