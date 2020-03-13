package dao

import java.sql.Date

import model.row._
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait Tables {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  lazy val users = TableQuery[UserTable]

  lazy val roles = TableQuery[RoleTable]

  lazy val userRoles = TableQuery[UserRoleTable]

  lazy val blocks = TableQuery[BlockTable]

  lazy val bills = TableQuery[BillTable]

  lazy val apartments = TableQuery[ApartmentTable]

  lazy val apartmentsOccupants = TableQuery[ApartmentOccupantTable]

  lazy val addresses = TableQuery[AddressTable]

  class UserTable(tag: Tag) extends Table[UserRow](tag, "user_t") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email", O.Unique)

    def enabled = column[Boolean]("enabled")

    def firstName = column[String]("first_name")

    def password = column[Option[String]]("password")

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

  class BlockTable(tag: Tag) extends Table[BlockRow](tag, "block_t") {

    def id = column[Option[Long]]("id", O.AutoInc, O.Unique, O.PrimaryKey)

    def addressId = column[Long]("address_id")

    override def * = (id, addressId) <> (BlockRow.tupled, BlockRow.unapply)
  }

  class BillTable(tag: Tag) extends Table[BillRow](tag: Tag, "bill_t") {

    def id = column[Long]("id", O.AutoInc, O.Unique, O.PrimaryKey)

    def amount = column[Double]("amount")

    def date = column[Date]("date")

    def `type` = column[String]("type")

    def apartmentId = column[Long]("apartment_id")

    override def * = (id, amount, date, `type`, apartmentId) <> (BillRow.tupled, BillRow.unapply)
  }

  class ApartmentTable(tag: Tag) extends Table[ApartmentRow](tag: Tag, "apartment_t") {

    def id = column[Option[Long]]("id", O.AutoInc, O.Unique, O.PrimaryKey)

    def number = column[Long]("number")

    def blockId = column[Long]("block_id")

    override def * = (id, number, blockId) <> (ApartmentRow.tupled, ApartmentRow.unapply)
  }

  class ApartmentOccupantTable(tag: Tag) extends Table[ApartmentOccupantRow](tag, "apartment_occupant_t") {

    def apartmentId = column[Long]("apartment_id")

    def occupantId = column[Long]("occupant_id")

    override def * = (apartmentId, occupantId) <> (ApartmentOccupantRow.tupled, ApartmentOccupantRow.unapply)
  }

  class AddressTable(tag: Tag) extends Table[AddressRow](tag, "address_t") {

    def id = column[Option[Long]]("id", O.AutoInc, O.Unique, O.PrimaryKey)

    def city = column[String]("city")

    def number = column[String]("number")

    def street = column[String]("street")

    def zipCode = column[String]("zip_code")

    override def * = (id, city, number, street, zipCode) <> (AddressRow.tupled, AddressRow.unapply)
  }


}
