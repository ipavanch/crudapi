package com.mansoor.rest.crudapi.server.paths

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import com.mansoor.rest.crudapi.server.backend.db.Operations
import com.mansoor.rest.crudapi.server.backend.db.dto.VaultDTO
import com.mansoor.rest.crudapi.utils.db.DBType
import com.mansoor.rest.crudapi.utils.json.JsonSupport
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media._
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.{POST, Path}

import scala.util.Try

@Path("/register")
case object RegisterVaultUser extends Directives with JsonSupport {
  @POST
  @Operation(
    summary = "Register CRUD API User",
    description = "Registers the JDBC details of CRUD API user in vault table!",
    parameters = Array(
      new Parameter(in = ParameterIn.HEADER, name = "X-Requested-By", description = "Name of the user/application that sent the HTTP request", required = true)
    ),
    requestBody = new RequestBody(
      description = "Payload to register the user for CRUD API usage in vault table",
      content = Array(
        new Content(
          mediaType = "application/json",
          schema = new Schema(
            name = "Vault JSON",
            implementation = classOf[VaultDTO]
          )
        )
      ),
      required = true
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "User successfully registered in the vault with id: {id}"),
      new ApiResponse(responseCode = "400", description = """{ "rejection" : "Request is missing required HTTP header 'X-Requested-By'", "refer": "Swagger API on /swagger endpoint for usage of routes!"}"""),
      new ApiResponse(responseCode = "500", description = "Internal Server Error"),
      new ApiResponse(responseCode = "501", description = "No support for dbType: {dbType} yet!, allowable values are: {dbTypes}")
    ),
    tags = Array("Register")
  )
  def route: Route = path("register") {
    post {
      entity(as[VaultDTO]) { v =>
        if(Try(DBType.values.contains(DBType.withName(v.dbType))).getOrElse(false)) {
          Operations.registerUser(v)
        }else {
          complete(HttpResponse(StatusCodes.NotImplemented,
            entity = HttpEntity(s"No support for dbType: ${v.dbType.toString} yet!, allowable values are: ${DBType.values.mkString(",")}"))
          )
        }
      }
    }
  }
}

