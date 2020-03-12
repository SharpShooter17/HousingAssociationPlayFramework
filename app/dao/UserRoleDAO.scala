package dao

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import model.row.UserRoleRow
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class UserRoleDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                           (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insert(row: UserRoleRow): Int = {
    val future = db.run(userRoles += row)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

}
