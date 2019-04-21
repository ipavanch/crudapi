package com.mansoor.rest.crudapi.utils.db

import doobie.util.update.Update0

trait CRUD {
  def table: String
  def create(): Update0
  def insert(record: Any): Update0
//  def read(): Unit
//  def update(): Unit
//  def delete(): Unit
}