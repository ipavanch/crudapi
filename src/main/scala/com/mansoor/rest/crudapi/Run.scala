package com.mansoor.rest.crudapi

import java.net.InetAddress
import akka.http.scaladsl.Http
import com.mansoor.rest.crudapi.server.Api
import com.mansoor.rest.crudapi.utils.args.CmdArgs
import com.mansoor.rest.crudapi.utils.config.ConfigLoader
import scopt.{OParser, OParserBuilder}

object Run extends App {
  val builder: OParserBuilder[CmdArgs] = OParser.builder[CmdArgs]
  val parser: OParser[Unit, CmdArgs] = {
    import builder._
    OParser.sequence(
      programName("REST CRUD API"),
      head("scopt", "4.x"),
      opt[InetAddress]('b', "bind-interface")
        .valueName("<bind-interface>")
        .optional()
        .action((x, c) => c.copy(bindInterface = x))
        .text("bind-interface is a required property, default., 127.0.0.1"),
      opt[Int]('p', "port")
        .valueName("<port>")
        .required()
        .action((x, c) => c.copy(port = x))
        .validate(v =>
          if(Range(1, 65537).contains(v)) success else failure("Input port should be within allowed range 1 to 65536")
        )
        .text("port number is a required property, eg., 8080"),
      opt[Unit]('s', "start")
        .required()
        .action((_, c) => c.copy(start = true))
        .text("start is a required flag to start the crudapi"),
      note(sys.props("line.separator"))
    )
  }

  OParser.parse(parser, args, CmdArgs()) match {
    case Some(config) =>
      System.setProperty("HOSTNAME", Option(System.getenv("HOSTNAME")).getOrElse(config.bindInterface.getHostName))
      hostname = config.bindInterface.getHostName
      port = config.port
      appConfig =  ConfigLoader.getConfig
      bindingFuture = Http().bindAndHandle(Api.routes, hostname, port)

      val url: String = s"${appConfig.frontend.scheme}://$hostname:$port"

      log.info(s"Server online at $url/")
      log.info(s"Swagger API accessible at $url/swagger")

    case None =>
      log.error("Received incorrect/bad arguments!")
  }
}
