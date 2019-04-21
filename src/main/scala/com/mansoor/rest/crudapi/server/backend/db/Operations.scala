package com.mansoor.rest.crudapi.server.backend.db

import cats.effect.{ContextShift, IO}
import com.mansoor.rest.crudapi.utils.config.ConfigLoader.DBConfig
import com.mansoor.rest.crudapi.utils.db.Connector
import com.mansoor.rest.crudapi.{appConfig, log}
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import doobie.implicits._
import cats.effect.IO
import com.mansoor.rest.crudapi.server.backend.db.dao.{UsageLogDAO, VaultDAO}
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
}
