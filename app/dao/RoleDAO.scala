package dao

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import model.row.RoleRow
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class RoleDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                       (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def findByRole(items: Set[String]): Iterable[RoleRow] = {
    val query = roles.filter(_.roleType.inSet(items))
    val future = db.run(query.result)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

}
