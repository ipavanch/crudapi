package com.mansoor.rest.crudapi.utils.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.mansoor.rest.crudapi.server.backend.db.dto.{UsageLogDTO, VaultDTO}
import com.mansoor.rest.crudapi.server.paths.payload.{SqlDeleteJson, SqlInsertJson, SqlUpdateJson}
import com.mansoor.rest.crudapi.utils.db.DBType
import spray.json._

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

  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any): JsValue = x match {
      case null => JsNull
      case n: Int => JsNumber(n)
      case s: String => JsString(s)
      case b: Boolean => if (b) JsTrue else JsFalse
      case l: List[Any] => JsArray(l.toVector.map(v => write(v)))
      case m: Map[String, Any] => JsObject(m.map { case (k, v) => (k, write(v)) })
    }
    def read(value: JsValue): Any = value match {
      case JsNull => null
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case JsTrue => true
      case JsFalse => false
      case JsArray(xs: Vector[JsValue]) => xs.toList.map { x => read(x) }
      case JsObject(fields: Map[String, JsValue]) => fields.map { case (k, jsv) => (k, read(jsv)) }
    }
  }

  implicit val dbTypeEnum: EnumJsonConverter[DBType.type] = new EnumJsonConverter(DBType)
  implicit val vaultDTO: RootJsonFormat[VaultDTO] = jsonFormat6(VaultDTO)
  implicit val usageLogDTO: RootJsonFormat[UsageLogDTO] = jsonFormat7(UsageLogDTO)
  implicit val sqlInsertJson: RootJsonFormat[SqlInsertJson] = jsonFormat4(SqlInsertJson)
  implicit val sqlUpdateJson: RootJsonFormat[SqlUpdateJson] = jsonFormat4(SqlUpdateJson)
  implicit val sqlDeleteJson: RootJsonFormat[SqlDeleteJson] = jsonFormat3(SqlDeleteJson)
}
