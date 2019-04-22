package com.mansoor.rest.crudapi.utils

package object db {

  def convertAnyToSqlType(v: Any): Any = {
    v match {
      case null => null
      case n: Int => n
      case s: String => s"'$s'"
      case b: Boolean => b
      case x => x
    }
  }
}
