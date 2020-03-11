package dao

import java.sql.Date
import java.util.concurrent.TimeUnit

import javax.inject.Inject
import model.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                       (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val users = TableQuery[UserTable]

  def all(implicit dbSession: Session = db.createSession()): Future[Seq[User]] = db.run(users.result)

  def insert(user: User): Future[Int] = db.run(users += user)

  def findByEmail(email: String): Option[User] = {
    val future = db.run(users.filter(_.email === email).result.headOption)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

  private class UserTable(tag: Tag) extends Table[User](tag, "user_t") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email", O.Unique)

    def enabled = column[Boolean]("enabled")

    def firstName = column[String]("first_name")

    def password = column[String]("password")

    def lastName = column[String]("last_name")

    def telephone = column[String]("telephone")

    def token = column[Option[String]]("token")

    def tokenExpirationDate = column[Option[Date]]("token_expiration_date")

    override def * = (id, email, enabled, firstName, password, lastName, telephone, token, tokenExpirationDate) <> (User.tupled, User.unapply)
  }

}
