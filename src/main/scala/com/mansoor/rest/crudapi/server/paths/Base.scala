package com.mansoor.rest.crudapi.server.paths

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.{GET, Path}

@Path("/")
case object Base extends Directives {
  @GET
  @Operation(
    summary = "Homepage",
    description = "Displays the welcome message",
    parameters = Array(
      new Parameter(in = ParameterIn.HEADER, name = "X-Requested-By", description = "Name of the user/application that sent the HTTP request", required = true)
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Welcome to RESTful CRUD API!"),
      new ApiResponse(responseCode = "400", description = """{ "rejection" : "Request is missing required HTTP header 'X-Requested-By'", "refer": "Swagger API on /swagger endpoint for usage of routes!"}"""),
      new ApiResponse(responseCode = "500", description = "Internal Server Error")
    ),
    tags = Array("Base")
  )
  def route: Route = pathSingleSlash {
    get {
      complete(HttpResponse(status = StatusCodes.OK, entity = HttpEntity("Welcome to RESTful CRUD API!")))
    }
  }
}
