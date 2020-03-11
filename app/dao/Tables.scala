package dao

import java.sql.Date

import model.row.{RoleRow, UserRoleRow, UserRow}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait Tables {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  val users = TableQuery[UserTable]

  val roles = TableQuery[RoleTable]

  val userRoles = TableQuery[UserRoleTable]

  class UserTable(tag: Tag) extends Table[UserRow](tag, "user_t") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email", O.Unique)

    def enabled = column[Boolean]("enabled")

    def firstName = column[String]("first_name")

    def password = column[String]("password")

    def lastName = column[String]("last_name")

    def telephone = column[String]("telephone")

    def token = column[Option[String]]("token")

    def tokenExpirationDate = column[Option[Date]]("token_expiration_date")

    override def * = (id, email, enabled, firstName, password, lastName, telephone, token, tokenExpirationDate) <> (UserRow.tupled, UserRow.unapply)
  }

  class RoleTable(tag: Tag) extends Table[RoleRow](tag, "role_t") {

    def id = column[Long]("id", O.PrimaryKey, O.Unique, O.AutoInc)

    def roleType = column[String]("role_type", O.Unique)

    override def * = (id, roleType) <> (RoleRow.tupled, RoleRow.unapply)
  }

  class UserRoleTable(tag: Tag) extends Table[UserRoleRow](tag, "user_role_t") {

    def userId = column[Long]("user_id")

    def roleId = column[Long]("role_id")

    override def * = (userId, roleId) <> (UserRoleRow.tupled, UserRoleRow.unapply)
  }


}
