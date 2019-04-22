package com.mansoor.rest.crudapi.utils.config

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigLoader {
  val APP_CONF_REF: String = "crudapi"
  val FRONTEND: String = "frontend"
  val BACKEND: String = "backend"
  val DB: String = "db"
  val SCHEME: String = "scheme"
  val DRIVER: String = "driver"
  val JDBC_URL: String = "jdbcURL"
  val USERNAME: String = "username"
  val PASSWORD: String = "password"
  val NAME: String = "name"
}

object ConfigLoader extends ConfigLoader {
  ConfigFactory.invalidateCaches()
  val sysConfig: Config = ConfigFactory.systemProperties()
    .withFallback(ConfigFactory.systemEnvironment())
  val config: Config = ConfigFactory.load()

  def getConfig: AppConfig = {
    val appConf: Config = config.getConfig(APP_CONF_REF).resolveWith(sysConfig)

    AppConfig(
      frontend = getFrontendConf(appConf.getConfig(FRONTEND)),
      backend = getBackendConf(appConf.getConfig(BACKEND))
    )
  }

  def getFrontendConf(conf: Config): FrontendConfig = {
    FrontendConfig(
      scheme = conf.getString(SCHEME)
    )
  }

  def getBackendConf(conf: Config): BackendConfig = {
    BackendConfig(
      db = getDBConfig(conf.getConfig(DB))
    )
  }

  def getDBConfig(conf: Config): DBConfig = {
    val dbConf: DBConfig = DBConfig(
      jdbcDriver = conf.getString(DRIVER),
      jdbcURL = conf.getString(JDBC_URL),
      username = conf.getString(USERNAME),
      password = conf.getString(PASSWORD),
      name = conf.getString(NAME)
    )
    require(dbConf.jdbcDriver.nonEmpty, s"Missing backend JDBC Driver in configuration, please set it via system env or system prop of BACKEND_JDBC_DRIVER")
    require(dbConf.jdbcURL.nonEmpty, s"Missing backend JDBC URL in configuration, please set it via system env or system prop of BACKEND_JDBC_URL")
    require(dbConf.username.nonEmpty, s"Missing backend JDBC Username in configuration, please set it via system env or system prop of BACKEND_JDBC_USERNAME")
    require(dbConf.password.nonEmpty, s"Missing backend JDBC Password in configuration, please set it via system env or system prop of BACKEND_JDBC_PASSWORD")
    require(dbConf.name.nonEmpty, s"Missing backend JDBC Database name in configuration, please set it via system env or system prop of BACKEND_DB")
    dbConf
  }

  case class AppConfig(frontend: FrontendConfig, backend: BackendConfig)
  case class FrontendConfig(scheme: String)
  case class BackendConfig(db: DBConfig)
  case class DBConfig(jdbcDriver: String, jdbcURL: String, username: String, password: String, name: String)
}
