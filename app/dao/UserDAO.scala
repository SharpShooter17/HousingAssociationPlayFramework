package dao

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import model.domain.User
import model.row.UserRow
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                       (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def all(implicit dbSession: Session = db.createSession()): Future[Seq[UserRow]] = db.run(users.result)

  def insert(user: UserRow): Future[Int] = db.run(users += user)

  def findByEmail(email: String): Option[User] = {
    val feature = db.run(getUserQuery(Option(email)).result)
      .map { data =>
        val byUser = data.groupBy(_._1._1)
        val usersWithRoles = byUser.map(entry => entry._1 -> entry._2.map(_._2.`type`))
        usersWithRoles.map {
          case (user, roles) =>
            User(
              id = user.id,
              email = user.email,
              firstName = user.firstName,
              lastName = user.lastName,
              telephone = user.telephone,
              roles = roles.toSet,
              enabled = user.enabled,
              hashPassword = user.password,
              token = user.token,
              tokenExpirationDate = user.tokenExpirationDate
            )
        }.headOption
      }

    Await.result(feature, FiniteDuration(10, TimeUnit.SECONDS))
  }

  private def getUserQuery(maybeEmail: Option[String] = None) = {
    val userQuery = maybeEmail match {
      case None => users
      case Some(email) => users.filter(_.email === email)
    }

    val withUserRolesQuery = for {
      (user, role) <- userQuery.join(userRoles).on((u, r) => u.id === r.userId)
    } yield (user, role)

    val withRoles = for {
      (userRole, role) <- withUserRolesQuery.join(roles).on(_._2.roleId === _.id)
    } yield (userRole, role)

    withRoles
  }

}
