package com.mansoor.rest.crudapi.utils.db

import cats.effect.IO
import doobie.util.update.Update0
import doobie.util.yolo.Yolo

trait CRUD {
  def yolo: Yolo[IO] = ???
  def table: String
  def create(): Update0
  def insert(record: Any): Update0
//  def update(): Unit
//  def delete(): Unit
}