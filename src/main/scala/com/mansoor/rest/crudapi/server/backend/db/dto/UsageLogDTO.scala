package com.mansoor.rest.crudapi.server.backend.db.dto

case class UsageLogDTO(vaultNs: String,
                       vaultUsr: String,
                       runMode: String,
                       operation: String,
                       entity: String,
                       status: Boolean,
                       msg: String)