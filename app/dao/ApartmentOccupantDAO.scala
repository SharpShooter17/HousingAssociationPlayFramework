package dao

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import model.row.{AddressRow, ApartmentOccupantRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class ApartmentOccupantDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                    (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def insert(row: ApartmentOccupantRow): Unit = {
    val future = db.run(apartmentsOccupants += row)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

  def delete(apartmentId: Long, occupantId: Long) = {
    val filtered = apartmentsOccupants.filter(row => row.occupantId === occupantId && row.apartmentId === apartmentId)
    val future = db.run(filtered.delete)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

}
