package com.mansoor.rest.crudapi.server.backend.db

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.server.Directives.complete
import cats.effect.ContextShift
import com.mansoor.rest.crudapi.utils.config.ConfigLoader.DBConfig
import com.mansoor.rest.crudapi.utils.db.{Connector, DBType, Driver, RowOps}
import com.mansoor.rest.crudapi.{appConfig, ec, log}
import com.mansoor.rest.crudapi.utils.db.convertAnyToSqlType
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import doobie.implicits._
import cats.effect.IO
import com.mansoor.rest.crudapi.server.backend.db.dao.{UsageLogDAO, VaultDAO}
import com.mansoor.rest.crudapi.server.backend.db.dto.{UsageLogDTO, VaultDTO}
import com.mansoor.rest.crudapi.server.paths.payload.{SqlDeleteJson, SqlInsertJson, SqlSelectJson, SqlUpdateJson}
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.util.yolo.Yolo

import scala.concurrent.{ExecutionContext, Future}

object Operations extends RowOps {

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

  def logUsage(u: UsageLogDTO): Unit = {
    val y: Yolo[IO] = xa.yolo
    import y._
    usageLogDAO.insert(u).quick.attempt.unsafeRunAsyncAndForget()
  }

  def selectSql(ns: String, user: String, sqlJson: SqlSelectJson, limit: Option[Int]): Future[Any] = {
    val vRec: Option[VaultDTO] = getVaultRec(ns, user)
    if(vRec.isDefined) {
      val dbConf: DBConfig = transform2DBConf(vRec.get, sqlJson.schema)
      val clientXA: Transactor[IO] = Connector.get(dbConf)
      selectRows(sqlJson.schema.trim, sqlJson.table.trim, sqlJson.cols, sqlJson.where, limit).nel.transact(clientXA).unsafeToFuture()
    }else {
      Future.failed[List[Any]](new IllegalArgumentException(s"Namespace $ns and User $user combination not found in ${vaultDAO.table} table!"))
    }
  }

  def insertSql(ns: String, user: String, sqlJson: SqlInsertJson): Future[List[Unit]] = {
    val vRec: Option[VaultDTO] = getVaultRec(ns, user)
    if(vRec.isDefined) {
      val dbConf: DBConfig = transform2DBConf(vRec.get, sqlJson.schema)
      val clientXA: Transactor[IO] = Connector.get(dbConf)
      val yolo: Yolo[IO] = clientXA.yolo
      import yolo._
      Future.sequence(sqlJson.rows.map(i => insertRow(sqlJson.schema.trim, sqlJson.table.trim, i).quick.unsafeToFuture()))
    }else {
      Future.failed[List[Unit]](new IllegalArgumentException(s"Namespace $ns and User $user combination not found in ${vaultDAO.table} table!"))
    }
  }

  def updateSql(ns: String, user: String, sqlJson: SqlUpdateJson): Future[Unit] = {
    val vRec: Option[VaultDTO] = getVaultRec(ns, user)
    if(vRec.isDefined) {
      val dbConf: DBConfig = transform2DBConf(vRec.get, sqlJson.schema)
      val clientXA: Transactor[IO] = Connector.get(dbConf)
      val yolo: Yolo[IO] = clientXA.yolo
      import yolo._
      updateRow(sqlJson.schema.trim, sqlJson.table.trim, sqlJson.set, sqlJson.where.trim).quick.unsafeToFuture()
    }else {
      Future.failed[Unit](new IllegalArgumentException(s"Namespace $ns and User $user combination not found in ${vaultDAO.table} table!"))
    }
  }

  def deleteSql(ns: String, user: String, sqlJson: SqlDeleteJson): Future[Unit] = {
    val vRec: Option[VaultDTO] = getVaultRec(ns, user)
    if(vRec.isDefined) {
      val dbConf: DBConfig = transform2DBConf(vRec.get, sqlJson.schema)
      val clientXA: Transactor[IO] = Connector.get(dbConf)
      val yolo: Yolo[IO] = clientXA.yolo
      import yolo._
      deleteRow(sqlJson.schema.trim, sqlJson.table.trim, sqlJson.where.trim).quick.unsafeToFuture()
    }else {
      Future.failed[Unit](new IllegalArgumentException(s"Namespace $ns and User $user combination not found in ${vaultDAO.table} table!"))
    }
  }

  override def selectRows(schema: String, table: String, cols: List[String], where: Option[String], limit: Option[Int]): Query0[String] = {
    val qb: StringBuilder = new StringBuilder()
    qb ++= s"SELECT ${cols.mkString(",")} FROM $schema.$table"
    if(where.isDefined) qb ++= s" WHERE ${where.get}"
    if(limit.isDefined) qb ++= s" LIMIT ${limit.get}"
    qb += ';'
    Fragment.const(qb.mkString).query[String]
  }

  override def insertRow(schema: String, table: String, row: Map[String, Any]): Update0 = {
    val query: String =
      s"""
         |INSERT INTO $schema.$table (${row.keys.mkString(",")}) VALUES (${row.values.map(convertAnyToSqlType).mkString(",")});
     """.stripMargin
    Fragment.const(query).update
  }

  override def updateRow(schema: String, table: String, set: Map[String, Any], where: String): Update0 = {
    val query: String =
      s"""
         |UPDATE $schema.$table
         |SET ${set.toList.map(i => s"${i._1}=${convertAnyToSqlType(i._2)}").mkString(",")}
         |WHERE $where;
     """.stripMargin
    Fragment.const(query).update
  }

  override def deleteRow(schema: String, table: String, where: String): Update0 = {
    val query: String =
      s"""
         |DELETE FROM $schema.$table
         |WHERE $where;
     """.stripMargin
    Fragment.const(query).update
  }

  private def getVaultRec(ns: String, user: String): Option[VaultDTO] = {
    try {
      Some(vaultDAO.read(ns, user).unique.transact(xa).unsafeRunSync())
    }catch {
      case ex: Throwable =>
        log.error(s"Namespace $ns and User $user combination not found in ${vaultDAO.table} table!", ex)
        None
    }
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
