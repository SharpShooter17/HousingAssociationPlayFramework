package dao

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import model.row.ApartmentRow
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class ApartmentDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                            (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private def insertQuery() = apartments returning apartments.map(_.id) into ((address, id) => address.copy(id = id))

  def insert(row: ApartmentRow): ApartmentRow = {
    val apartmentWithId = insertQuery() += row
    val future = db.run(apartmentWithId)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

}
