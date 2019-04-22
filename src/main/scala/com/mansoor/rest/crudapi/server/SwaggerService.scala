package com.mansoor.rest.crudapi.server

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.{Contact, Info}
import com.mansoor.rest.crudapi.server.paths.{Base, RegisterVaultUser, SqlSubmit}
import com.mansoor.rest.crudapi.{appConfig, hostname, port}

case object SwaggerService extends SwaggerHttpService {

  override val host = s"$hostname:$port"
  override val basePath = "/"
  override val unwantedDefinitions: Seq[String] = Seq("Function1",
    "Function1RequestContextFutureRouteResult",
    "ListString",
    "ListMapStringString",
    "MapStringString"
  )

  override def apiClasses: Set[Class[_]] = Set(
    Base.getClass,
    RegisterVaultUser.getClass,
    SqlSubmit.getClass
  )

  override def schemes: List[String] = List(appConfig.frontend.scheme)

  override def apiDocsPath: String = "api-docs"

  override def info: Info = Info(
    description = "Swagger for RESTful CRUD API",
    version = "",
    title = "RESTful CRUD API",
    termsOfService = "",
    contact = Option(Contact(
      name = "Mansoor Baba Shaik",
      url = "https://www.linkedin.com/in/mansoor-baba-shaik-57bb92174/",
      email = "mansoorbabashaik@outlook.com"
    )),
    license = None,
    vendorExtensions = Map.empty
  )
}
