package com.mansoor.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.RejectionHandler
import akka.stream.ActorMaterializer
import com.mansoor.rest.crudapi.utils.config.ConfigLoader.AppConfig
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

package object crudapi {
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  implicit def myRejectHandler: RejectionHandler = {
    RejectionHandler.default
      .mapRejectionResponse({
        case res@HttpResponse(_, _, ent: HttpEntity.Strict, _) =>
          val msg = ent.data.utf8String.replaceAll("\"", """\"""")
          res.copy(entity = HttpEntity(ContentTypes.`application/json`,
            s"""
               |{
               |  "rejection" : "$msg",
               |  "refer": "Swagger API on /swagger endpoint for usage of routes!"
               |}
             """.stripMargin
          ))

        case x => x
      })
  }

  implicit val system: ActorSystem = ActorSystem("crudapi")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  var hostname: String = _
  var port: Int = _
  var bindingFuture: Future[ServerBinding] = _

  var appConfig: AppConfig = _

  val `X-Requested-By`: String = "X-Requested-By"
  val `select`: String = "select"
}
