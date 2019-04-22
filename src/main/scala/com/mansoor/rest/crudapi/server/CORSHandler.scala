package com.mansoor.rest.crudapi.server

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}

trait CORSHandler {
  private val corsResponseHeaders = List(
    `Cache-Control`(CacheDirectives.`no-cache`),
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Origin", "X-Requested-By", "Content-Type", "Accept", "Authorization")
  )
  private def addAccessControlHeaders: Directive0 = respondWithHeaders(corsResponseHeaders)
  private def preFlightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).withHeaders(`Access-Control-Allow-Methods`(OPTIONS, GET, POST, PUT, DELETE)))
  }
  def corsHandler(r: Route): Route = addAccessControlHeaders {
    preFlightRequestHandler ~ r
  }
  def addCORSHeaders(response: HttpResponse): HttpResponse = response.withHeaders(corsResponseHeaders)
}
