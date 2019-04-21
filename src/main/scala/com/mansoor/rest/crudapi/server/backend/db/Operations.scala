package com.mansoor.rest.crudapi.server.backend.db

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.StandardRoute
import cats.effect.{ContextShift, IO}
import com.mansoor.rest.crudapi.utils.config.ConfigLoader.DBConfig
import com.mansoor.rest.crudapi.utils.db.Connector
import com.mansoor.rest.crudapi.{appConfig, log}
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import doobie.implicits._
import cats.effect.IO
import com.mansoor.rest.crudapi.server.backend.db.dao.{UsageLogDAO, VaultDAO}
import com.mansoor.rest.crudapi.server.backend.db.dto.VaultDTO
import com.mansoor.rest.crudapi.server.paths.RegisterVaultUser.complete
import doobie.util.yolo.Yolo

import scala.concurrent.ExecutionContext

object Operations {

  private val backendDB: DBConfig = appConfig.backend.db
  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  private val xa: Transactor[IO] = Connector.get(backendDB)
  val y: Yolo[IO] = xa.yolo
  import y._

  def checkDBConnect(): Unit = {
    try {
      val query = Fragment.const(s"SELECT 1 AS result FROM pg_database WHERE datname='${backendDB.name}'").query[Int].unique
      query.transact(xa).unsafeRunSync()
      log.info(s"Connected to backend server database! - ${backendDB.name}")
    }catch {
      case ex: Throwable =>
        log.error(s"Unable to connect to backend server database! - ${backendDB.name}")
        sys.exit(1)
    }
  }

  val vaultDAO: VaultDAO = new VaultDAO()
  val usageLogDAO: UsageLogDAO = new UsageLogDAO()

  def init(): Unit = {
    vaultDAO.create().quick.attempt.unsafeRunSync() match {
        case Left(th) => log.error(s"Unable to create table ${vaultDAO.table}!", th)
        case Right(x) => log.info(s"Table created successfully: ${vaultDAO.table}!", x)
    }
    usageLogDAO.create().quick.attempt.unsafeRunSync() match {
      case Left(th) => log.error(s"Unable to create table ${usageLogDAO.table}!", th)
      case Right(x) => log.info(s"Table created successfully: ${usageLogDAO.table}!", x)
    }
  }

  def registerUser(v: VaultDTO): StandardRoute = {
    vaultDAO.insert(v).quick.attempt.unsafeRunSync() match {
      case Left(ex) => complete(HttpResponse(StatusCodes.NotAcceptable, entity = HttpEntity(ex.toString)))
      case Right(x) => complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(s"User registered successfully with namespace: ${v.namespace}")))
    }
  }
}
