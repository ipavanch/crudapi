package com.mansoor.rest.crudapi.utils.db

import cats.effect.{ContextShift, IO}
import com.mansoor.rest.crudapi.utils.config.ConfigLoader.DBConfig
import doobie.util.transactor.Transactor
import scala.concurrent.ExecutionContext

object Connector {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def get(db: DBConfig): Transactor[IO] = {
    Transactor.fromDriverManager[IO](
      driver = db.jdbcDriver,
      url = db.jdbcURL,
      user = db.username,
      pass = db.password
    )
  }

}
