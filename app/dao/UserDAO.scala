package dao

import java.util.concurrent.TimeUnit

import exceptions.AppException
import javax.inject.{Inject, Singleton}
import model.domain.User
import model.form.UserForm
import model.row.{RoleRow, UserRoleRow, UserRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

@Singleton
class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, roleDAO: RoleDAO, userRoleDAO: UserRoleDAO)
                       (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def all(): Iterable[User] = {
    val future = db.run(getUserQuery().result).map(mapUser)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

  private def insertQuery() = users returning users.map(_.id) into ((user, id) => user.copy(id = id))

  def insert(user: UserForm): UserRow = {
    val userRow = UserRow(
      email = user.email,
      firstName = user.firstName,
      lastName = user.lastName,
      telephone = user.telephone
    )
    val userId = insertQuery += userRow
    val future = db.run(userId)
    val newUser = Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
    roleDAO.findByRole(user.roles)
      .map(role => UserRoleRow(newUser.id.getOrElse(throw AppException()), role.id))
      .foreach(userRoleDAO.insert)
    newUser
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
      (user, role) <- userQuery.joinLeft(userRoles).on((u, r) => u.id === r.userId)
    } yield (user, role)

    for {
      (userRole, role) <- withUserRolesQuery.joinLeft(roles).on(_._2.map(_.roleId) === _.id)
    } yield (userRole, role)
  }

  private def mapUser(user: Seq[((UserRow, Option[UserRoleRow]), Option[RoleRow])]): immutable.Iterable[User] = {
    val byUser = user.groupBy(_._1._1)
    val usersWithRoles = byUser.map(entry => entry._1 -> entry._2.flatMap(_._2.map(_.`type`)))
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
