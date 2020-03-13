package dao

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import model.row.AddressRow
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.FiniteDuration

@Singleton
class AddressDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                          (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private def insertQuery() = addresses returning addresses.map(_.id) into ((address, id) => address.copy(id = id))

  def insert(address: AddressRow): AddressRow = {
    val addressId = insertQuery() += address
    val future = db.run(addressId)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

}
