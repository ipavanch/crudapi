package com.mansoor.rest.crudapi.server.paths

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import scala.util.{Failure, Success}
import akka.http.scaladsl.server.{Directives, Route}
import com.mansoor.rest.crudapi.server.backend.db.Operations
import com.mansoor.rest.crudapi.server.paths.payload.{SqlDeleteJson, SqlInsertJson, SqlSelectJson, SqlUpdateJson}
import com.mansoor.rest.crudapi.utils.json.JsonSupport
import io.swagger.v3.oas.annotations.enums.{Explode, ParameterIn, ParameterStyle}
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import javax.ws.rs._
import com.mansoor.rest.crudapi.{`X-Requested-By`, `select`}
import com.mansoor.rest.crudapi.server.backend.db.dto.UsageLogDTO

@Path("/sql/{vaultNamespace}")
case object SqlSubmit extends Directives with JsonSupport {
  @GET
  @Operation(
    summary = "Fetch SQL select all",
    description = "Fetches all records",
    parameters = Array(
      new Parameter(in = ParameterIn.HEADER, name = "X-Requested-By", description = "Name of the user/application that sent the HTTP request", required = true),
      new Parameter(in = ParameterIn.PATH, name = "vaultNamespace", description = "Namespace of the CRUD API user in vault table to extract connection details", required = true),
      new Parameter(in = ParameterIn.QUERY, name = "select", description = "Select query json", required = true, schema = new Schema(implementation = classOf[SqlSelectJson]))
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "[{rows}]"),
      new ApiResponse(responseCode = "400", description = """{ "rejection" : "Request is missing required HTTP header 'X-Requested-By'", "refer": "Swagger API on /swagger endpoint for usage of routes!"}"""),
      new ApiResponse(responseCode = "409", description = "{exception}"),
      new ApiResponse(responseCode = "500", description = "Internal Server Error")
    ),
    tags = Array("SQL Select All")
  )
  def selectAllRoute: Route = path("sql" / Segment) { namespace =>
    get {
      headerValueByName(`X-Requested-By`) { user =>
        parameters('schema, 'table, 'cols, 'where.?) { (s, t, c, w) =>
          val sql: SqlSelectJson = SqlSelectJson.tupled(s, t, c.split(",").toList, w)
          val udto: UsageLogDTO = UsageLogDTO(
            vaultNs = namespace,
            vaultUsr = user,
            runMode = "sql",
            operation = "select",
            entity = sql.table,
            status = false,
            msg = ""
          )
          onComplete(Operations.selectSql(namespace, user, sql, None)) {
            case Success(v) =>
              val message: String = v.toString
              Operations.logUsage(udto.copy(status = true, msg = message))
              complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(message)))
            case Failure(ex) =>
              val message: String = ex.toString
              Operations.logUsage(udto.copy(status = false, msg = message))
              complete(HttpResponse(StatusCodes.Conflict, entity = HttpEntity(message)))
          }
        }
      }
    }
  }

  @Path("/{limit}")
  @GET
  @Operation(
    summary = "Fetch SQL select limit",
    description = "Fetches limited records",
    parameters = Array(
      new Parameter(in = ParameterIn.HEADER, name = "X-Requested-By", description = "Name of the user/application that sent the HTTP request", required = true),
      new Parameter(in = ParameterIn.PATH, name = "vaultNamespace", description = "Namespace of the CRUD API user in vault table to extract connection details", required = true),
      new Parameter(in = ParameterIn.PATH, name = "limit", description = "Number of records to fetch", required = false, schema = new Schema(implementation = classOf[Int])),
      new Parameter(in = ParameterIn.QUERY, name = "select", description = "Select query json", required = true, schema = new Schema(implementation = classOf[SqlSelectJson]))
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "[{rows}]"),
      new ApiResponse(responseCode = "400", description = """{ "rejection" : "Request is missing required HTTP header 'X-Requested-By'", "refer": "Swagger API on /swagger endpoint for usage of routes!"}"""),
      new ApiResponse(responseCode = "409", description = "{exception}"),
      new ApiResponse(responseCode = "500", description = "Internal Server Error")
    ),
    tags = Array("SQL Select Limit")
  )
  def selectLimitRoute: Route = path("sql" / Segment / IntNumber) { (namespace, limit) =>
    get {
      headerValueByName(`X-Requested-By`) { user =>
        parameters('schema, 'table, 'cols, 'where.?) { (s, t, c, w) =>
          val sql: SqlSelectJson = SqlSelectJson.tupled(s, t, c.split(",").toList, w)
          val udto: UsageLogDTO = UsageLogDTO(
            vaultNs = namespace,
            vaultUsr = user,
            runMode = "sql",
            operation = "select",
            entity = sql.table,
            status = false,
            msg = ""
          )
          onComplete(Operations.selectSql(namespace, user, sql, Option(limit))) {
            case Success(v) =>
              val message: String = v.toString
              Operations.logUsage(udto.copy(status = true, msg = message))
              complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(message)))
            case Failure(ex) =>
              val message: String = ex.toString
              Operations.logUsage(udto.copy(status = false, msg = message))
              complete(HttpResponse(StatusCodes.Conflict, entity = HttpEntity(message)))
          }
        }
      }
    }
  }

  @POST
  @Operation(
    summary = "Submits SQL insert payload",
    description = "Submits SQL payload for a namespace to insert records",
    parameters = Array(
      new Parameter(in = ParameterIn.HEADER, name = "X-Requested-By", description = "Name of the user/application that sent the HTTP request", required = true),
      new Parameter(in = ParameterIn.PATH, name = "vaultNamespace", description = "Namespace of the CRUD API user in vault table to extract connection details", required = true)
    ),
    requestBody = new RequestBody(
      description = "Payload to perform insert operation for a client on specified namespace",
      content = Array(
        new Content(
          mediaType = "application/json",
          schema = new Schema(
            name = "SQL Insert JSON",
            implementation = classOf[SqlInsertJson]
          )
        )
      ),
      required = true
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Records inserted successfully into table {table} !"),
      new ApiResponse(responseCode = "400", description = """{ "rejection" : "Request is missing required HTTP header 'X-Requested-By'", "refer": "Swagger API on /swagger endpoint for usage of routes!"}"""),
      new ApiResponse(responseCode = "409", description = "{exception}"),
      new ApiResponse(responseCode = "500", description = "Internal Server Error")
    ),
    tags = Array("SQL Insert")
  )
  def insertRoute: Route = path("sql" / Segment) { namespace =>
    post {
      headerValueByName(`X-Requested-By`) { user =>
        entity(as[SqlInsertJson]) { sql =>
          val udto: UsageLogDTO = UsageLogDTO(
            vaultNs = namespace,
            vaultUsr = user,
            runMode = "sql",
            operation = "insert",
            entity = sql.table,
            status = false,
            msg = ""
          )
          onComplete(Operations.insertSql(namespace, user, sql)) {
            case Success(v) =>
              val message: String = s"Records inserted successfully into table ${sql.table} !"
              Operations.logUsage(udto.copy(status = true, msg = message))
              complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(message)))
            case Failure(ex) =>
              val message: String = ex.toString
              Operations.logUsage(udto.copy(status = false, msg = message))
              complete(HttpResponse(StatusCodes.Conflict, entity = HttpEntity(message)))
          }
        }
      }
    }
  }

  @PUT
  @Operation(
    summary = "Submits SQL update payload",
    description = "Submits SQL payload for a namespace to update records",
    parameters = Array(
      new Parameter(in = ParameterIn.HEADER, name = "X-Requested-By", description = "Name of the user/application that sent the HTTP request", required = true),
      new Parameter(in = ParameterIn.PATH, name = "vaultNamespace", description = "Namespace of the CRUD API user in vault table to extract connection details", required = true)
    ),
    requestBody = new RequestBody(
      description = "Payload to perform update operation for a client on specified namespace",
      content = Array(
        new Content(
          mediaType = "application/json",
          schema = new Schema(
            name = "SQL Update JSON",
            implementation = classOf[SqlUpdateJson]
          )
        )
      ),
      required = true
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Records updated successfully in table {table} !"),
      new ApiResponse(responseCode = "400", description = """{ "rejection" : "Request is missing required HTTP header 'X-Requested-By'", "refer": "Swagger API on /swagger endpoint for usage of routes!"}"""),
      new ApiResponse(responseCode = "409", description = "{exception}"),
      new ApiResponse(responseCode = "500", description = "Internal Server Error")
    ),
    tags = Array("SQL Update")
  )
  def updateRoute: Route = path("sql" / Segment) { namespace =>
    put {
      headerValueByName(`X-Requested-By`) { user =>
        entity(as[SqlUpdateJson]) { sql =>
          val udto: UsageLogDTO = UsageLogDTO(
            vaultNs = namespace,
            vaultUsr = user,
            runMode = "sql",
            operation = "update",
            entity = sql.table,
            status = false,
            msg = ""
          )
          onComplete(Operations.updateSql(namespace, user, sql)) {
            case Success(v) =>
              val message: String = s"Records updated successfully in table ${sql.table} !"
              Operations.logUsage(udto.copy(status = true, msg = message))
              complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(message)))
            case Failure(ex) =>
              val message: String = ex.toString
              Operations.logUsage(udto.copy(status = false, msg = message))
              complete(HttpResponse(StatusCodes.Conflict, entity = HttpEntity(message)))
          }
        }
      }
    }
  }

  @DELETE
  @Operation(
    summary = "Submits SQL delete payload",
    description = "Submits SQL payload for a namespace to delete records",
    parameters = Array(
      new Parameter(in = ParameterIn.HEADER, name = "X-Requested-By", description = "Name of the user/application that sent the HTTP request", required = true),
      new Parameter(in = ParameterIn.PATH, name = "vaultNamespace", description = "Namespace of the CRUD API user in vault table to extract connection details", required = true)
    ),
    requestBody = new RequestBody(
      description = "Payload to perform delete operation for a client on specified namespace",
      content = Array(
        new Content(
          mediaType = "application/json",
          schema = new Schema(
            name = "SQL Delete JSON",
            implementation = classOf[SqlDeleteJson]
          )
        )
      ),
      required = true
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Records deleted successfully on table {table} where {where} !"),
      new ApiResponse(responseCode = "400", description = """{ "rejection" : "Request is missing required HTTP header 'X-Requested-By'", "refer": "Swagger API on /swagger endpoint for usage of routes!"}"""),
      new ApiResponse(responseCode = "409", description = "{exception}"),
      new ApiResponse(responseCode = "500", description = "Internal Server Error")
    ),
    tags = Array("SQL Delete")
  )
  def deleteRoute: Route = path("sql" / Segment) { namespace =>
    delete {
      headerValueByName(`X-Requested-By`) { user =>
        entity(as[SqlDeleteJson]) { sql =>
          val udto: UsageLogDTO = UsageLogDTO(
            vaultNs = namespace,
            vaultUsr = user,
            runMode = "sql",
            operation = "delete",
            entity = sql.table,
            status = false,
            msg = ""
          )
          onComplete(Operations.deleteSql(namespace, user, sql)) {
            case Success(v) =>
              val message: String = s"Records deleted successfully on table ${sql.table} where ${sql.where} !"
              Operations.logUsage(udto.copy(status = true, msg = message))
              complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(message)))
            case Failure(ex) =>
              val message: String = ex.toString
              Operations.logUsage(udto.copy(status = false, msg = message))
              complete(HttpResponse(StatusCodes.Conflict, entity = HttpEntity(ex.toString)))
          }
        }
      }
    }
  }

  def route: Route = selectAllRoute ~ selectLimitRoute ~ insertRoute ~ updateRoute ~ deleteRoute
}