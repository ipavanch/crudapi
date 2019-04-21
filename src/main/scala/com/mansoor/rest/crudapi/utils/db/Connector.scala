package com.mansoor.rest.crudapi.utils.db

import cats.effect.{ContextShift, IO}
import com.mansoor.rest.crudapi.utils.config.ConfigLoader.DBConfig
import doobie.util.transactor.Transactor

object Connector {
  def get(db: DBConfig)(implicit cs: ContextShift[IO]): Transactor[IO] = {
    Transactor.fromDriverManager[IO](
      driver = db.jdbcDriver,
      url = db.jdbcURL,
      user = db.username,
      pass = db.password
    )
  }
}
