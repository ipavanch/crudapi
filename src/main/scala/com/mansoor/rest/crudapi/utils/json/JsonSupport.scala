package com.mansoor.rest.crudapi.utils.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.mansoor.rest.crudapi.server.backend.db.dto.VaultDTO
import com.mansoor.rest.crudapi.server.paths.payload.{SqlDeleteJson, SqlInsertJson, SqlUpdateJson}
import com.mansoor.rest.crudapi.utils.db.DBType
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  class EnumJsonConverter[T <: scala.Enumeration](enu: T) extends RootJsonFormat[T#Value] {
    override def write(obj: T#Value): JsValue = JsString(obj.toString)

    override def read(json: JsValue): T#Value = {
      json match {
        case JsString(txt) => enu.withName(txt)
        case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
      }
    }
  }

  implicit val dbTypeEnum: EnumJsonConverter[DBType.type] = new EnumJsonConverter(DBType)
  implicit val vaultDTO: RootJsonFormat[VaultDTO] = jsonFormat6(VaultDTO)
  implicit val sqlInsertJson: RootJsonFormat[SqlInsertJson] = jsonFormat4(SqlInsertJson)
  implicit val sqlUpdateJson: RootJsonFormat[SqlUpdateJson] = jsonFormat4(SqlUpdateJson)
  implicit val sqlDeleteJson: RootJsonFormat[SqlDeleteJson] = jsonFormat3(SqlDeleteJson)
}
