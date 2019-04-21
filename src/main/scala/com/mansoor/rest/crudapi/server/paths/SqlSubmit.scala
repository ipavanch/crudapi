package com.mansoor.rest.crudapi.server.paths

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import scala.util.{Failure, Success}
import akka.http.scaladsl.server.{Directives, Route}
import com.mansoor.rest.crudapi.server.backend.db.Operations
import com.mansoor.rest.crudapi.server.paths.payload.{SqlDeleteJson, SqlInsertJson, SqlUpdateJson}
import com.mansoor.rest.crudapi.utils.json.JsonSupport
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import javax.ws.rs.{DELETE, POST, PUT, Path}

@Path("/sql/{vaultNamespace}")
case object SqlSubmit extends Directives with JsonSupport {
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
      new ApiResponse(responseCode = "200", description = "Records inserted successfully into table {table}"),
      new ApiResponse(responseCode = "400", description = """{ "rejection" : "Request is missing required HTTP header 'X-Requested-By'", "refer": "Swagger API on /swagger endpoint for usage of routes!"}"""),
      new ApiResponse(responseCode = "404", description = "Namespace {vaultNamespace} not found in {vault} table!"),
      new ApiResponse(responseCode = "500", description = "Internal Server Error")
    ),
    tags = Array("SQL Insert")
  )
  def insertRoute: Route = path("sql" / Segment) { namespace =>
    post {
      entity(as[SqlInsertJson]) { sql =>
        onComplete(Operations.insertSql(namespace.trim, sql)) {
          case Success(v) => complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(s"Records inserted successfully into table ${sql.table}!")))
          case Failure(ex) => complete(HttpResponse(StatusCodes.Conflict, entity = HttpEntity(ex.toString)))
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
      new ApiResponse(responseCode = "200", description = "Records updated successfully in table {table}"),
      new ApiResponse(responseCode = "400", description = """{ "rejection" : "Request is missing required HTTP header 'X-Requested-By'", "refer": "Swagger API on /swagger endpoint for usage of routes!"}"""),
      new ApiResponse(responseCode = "404", description = "Namespace {vaultNamespace} not found in {vault} table!"),
      new ApiResponse(responseCode = "500", description = "Internal Server Error")
    ),
    tags = Array("SQL Update")
  )
  def updateRoute: Route = path("sql" / Segment) { namespace =>
    put {
      entity(as[SqlUpdateJson]) { sql =>
        onComplete(Operations.updateSql(namespace.trim, sql)) {
          case Success(v) => complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(s"Records inserted successfully into table ${sql.table}!")))
          case Failure(ex) => complete(HttpResponse(StatusCodes.Conflict, entity = HttpEntity(ex.toString)))
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
      new ApiResponse(responseCode = "200", description = "Record submission success for operation: {operation}"),
      new ApiResponse(responseCode = "400", description = """{ "rejection" : "Request is missing required HTTP header 'X-Requested-By'", "refer": "Swagger API on /swagger endpoint for usage of routes!"}"""),
      new ApiResponse(responseCode = "404", description = "Namespace {vaultNamespace} not found in {vault} table!"),
      new ApiResponse(responseCode = "500", description = "Internal Server Error")
    ),
    tags = Array("SQL Delete")
  )
  def deleteRoute: Route = path("sql" / Segment) { namespace =>
    delete {
      entity(as[SqlDeleteJson]) { sql =>
        Operations.deleteSql(namespace.trim, sql)
      }
    }
  }

  def route: Route = insertRoute ~ updateRoute ~ deleteRoute
}