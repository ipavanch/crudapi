package com.mansoor.rest.crudapi.server.backend.db.dto

case class VaultDTO(namespace: String,
                    user: String,
                    dbType: String,
                    jdbcURL: String,
                    jdbcUser: String,
                    jdbcPass: String)
