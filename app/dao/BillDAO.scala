package dao

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import model.row.BillRow
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class BillDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                       (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private def insertQuery() = bills returning bills.map(_.id) into ((bill, id) => bill.copy(id = id))

  def insert(bill: BillRow): BillRow = {
    val billWithId = insertQuery() += bill
    val future = db.run(billWithId)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

}
