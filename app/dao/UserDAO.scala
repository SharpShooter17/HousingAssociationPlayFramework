package dao

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import model.domain.User
import model.form.UserForm
import model.row.{RoleRow, UserRoleRow, UserRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import security.PasswordService
import slick.jdbc.JdbcProfile

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                       (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def all(): Iterable[User] = {
    val future = db.run(getUserQuery().result).map(mapUser)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

  def insert(user: UserForm): Future[Int] = {
    val session = db.createSession()
    val userRow = UserRow(
      email = user.email,
      firstName = user.firstName,
      password = PasswordService.createPassword(user.password),
      lastName = user.lastName,
      telephone = user.telephone
    )
    val result = db.run(users += userRow)
    session.close()
    result
  }

  def findByEmail(email: String): Option[User] = {
    val future = db.run(getUserQuery(Option(email)).result).map(mapUser)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS)).headOption
  }

  private def getUserQuery(maybeEmail: Option[String] = None) = {
    val userQuery = maybeEmail match {
      case None => users
      case Some(email) => users.filter(_.email === email)
    }

    joinRoles(userQuery)
  }

  private def joinRoles(userQuery: Query[UserTable, UserRow, Seq]) = {
    val withUserRolesQuery = for {
      (user, role) <- userQuery.join(userRoles).on((u, r) => u.id === r.userId)
    } yield (user, role)

    for {
      (userRole, role) <- withUserRolesQuery.join(roles).on(_._2.roleId === _.id)
    } yield (userRole, role)
  }

  private def mapUser(user: Seq[((UserRow, UserRoleRow), RoleRow)]): immutable.Iterable[User] = {
    val byUser = user.groupBy(_._1._1)
    val usersWithRoles = byUser.map(entry => entry._1 -> entry._2.map(_._2.`type`))
    usersWithRoles.map {
      case (user, roles) =>
        User(
          id = user.id.getOrElse(-1),
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
    }
  }

}
