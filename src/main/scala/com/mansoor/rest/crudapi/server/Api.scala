package com.mansoor.rest.crudapi.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.mansoor.rest.crudapi.server.paths.Base
import com.mansoor.rest.crudapi.utils.json.JsonSupport
import com.mansoor.rest.crudapi.{`X-Requested-By`, appConfig}

object Api extends CORSHandler with JsonSupport {
  def routes: Route = {
    scheme(appConfig.frontend.scheme) {
      SwaggerService.routes ~ getFromResourceDirectory("swagger") ~
      path("swagger") {
        get {
          redirect("/swagger-ui/index.html", StatusCodes.PermanentRedirect)
        }
      } ~
      headerValueByName(`X-Requested-By`){ reqBy =>
        corsHandler(
          respondWithHeader(RawHeader(`X-Requested-By`, reqBy)) {
            Base.route
          }
        )
      }
    }
  }
}
