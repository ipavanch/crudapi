package com.mansoor.rest.crudapi.server.backend.db.dto

case class UsageLogDTO(vaultNs: String,
                       vaultUsr: String,
                       client: String,
                       runMode: String,
                       operation: String,
                       entity: String,
                       entryTs: String)