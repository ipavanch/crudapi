package com.mansoor.rest.crudapi.server.backend.db

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.server.Directives.complete
import cats.effect.ContextShift
import com.mansoor.rest.crudapi.utils.config.ConfigLoader.DBConfig
import com.mansoor.rest.crudapi.utils.db.{Connector, DBType, Driver}
import com.mansoor.rest.crudapi.{appConfig, ec, log}
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import doobie.implicits._
import cats.effect.IO
import com.mansoor.rest.crudapi.server.backend.db.dao.{UsageLogDAO, VaultDAO}
import com.mansoor.rest.crudapi.server.backend.db.dto.VaultDTO
import com.mansoor.rest.crudapi.server.paths.payload.{SqlDeleteJson, SqlInsertJson, SqlUpdateJson}
import doobie.util.update.Update0
import doobie.util.yolo.Yolo
import scala.concurrent.{ExecutionContext, Future}

object Operations {

  private val backendDB: DBConfig = appConfig.backend.db
  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  private val xa: Transactor[IO] = Connector.get(backendDB)

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
    val y: Yolo[IO] = xa.yolo
    import y._
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
    val y: Yolo[IO] = xa.yolo
    import y._
    vaultDAO.insert(v).quick.attempt.unsafeRunSync() match {
      case Left(ex) => complete(HttpResponse(StatusCodes.NotAcceptable, entity = HttpEntity(ex.toString)))
      case Right(x) => complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(s"User registered successfully with namespace: ${v.namespace}")))
    }
  }

  def insertSql(ns: String, sqlJson: SqlInsertJson): Future[List[Unit]] = {
    val vRec: Option[VaultDTO] = getVaultRec(ns)
    if(vRec.isDefined) {
      val dbConf: DBConfig = transform2DBConf(vRec.get, sqlJson.schema)
      val clientXA: Transactor[IO] = Connector.get(dbConf)
      val yolo: Yolo[IO] = clientXA.yolo
      import yolo._
      Future.sequence(sqlJson.rows.map(i => insertRow(sqlJson.schema, sqlJson.table, i).quick.unsafeToFuture()))
    }else {
      Future.failed[List[Unit]](new IllegalArgumentException(s"Namespace $ns not found in ${vaultDAO.table} table!"))
    }
  }

  def updateSql(ns: String, sqlJson: SqlUpdateJson): StandardRoute = {
    val vRec: Option[VaultDTO] = getVaultRec(ns)
    if(vRec.isDefined) {
      complete(HttpResponse(StatusCodes.OK, entity = sqlJson.toString))
    }else {
      nsNotFound(ns)
    }
  }

  def deleteSql(ns: String, sqlJson: SqlDeleteJson): StandardRoute = {
    val vRec: Option[VaultDTO] = getVaultRec(ns)
    if(vRec.isDefined) {
      complete(HttpResponse(StatusCodes.OK, entity = sqlJson.toString))
    }else {
      nsNotFound(ns)
    }
  }

  private def insertRow(schema: String, table: String, row: Map[String, String]): Update0 = {
    Fragment.const(
      s"""
       |INSERT INTO $schema.$table (${row.keys.mkString(",")}) VALUES (${row.values.mkString(",")});
     """.stripMargin
    ).update
  }

  private def getVaultRec(ns: String): Option[VaultDTO] = {
    try {
      Some(vaultDAO.read(ns).unique.transact(xa).unsafeRunSync())
    }catch {
      case ex: Throwable =>
        log.error(s"Namespace $ns not found in ${vaultDAO.table} table!", ex)
        None
    }
  }

  private def nsNotFound(ns: String): StandardRoute = {
    complete(HttpResponse(StatusCodes.NotFound, entity = s"Namespace $ns not found in ${vaultDAO.table} table!"))
  }

  private def transform2DBConf(vRec: VaultDTO, db: String): DBConfig = {
    DBConfig(
      jdbcDriver = Driver.get(DBType.withName(vRec.dbType)),
      jdbcURL = vRec.jdbcURL,
      username = vRec.jdbcUser,
      password = vRec.jdbcPass,
      name = db
    )
  }
}
