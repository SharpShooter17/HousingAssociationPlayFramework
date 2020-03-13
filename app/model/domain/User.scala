package model.domain

import java.sql.Date

import security.PasswordService

case class User(id: Long,
                email: String,
                firstName: String,
                lastName: String,
                telephone: String,
                roles: Set[String] = Set.empty,
                enabled: Boolean,
                hashPassword: Option[String] = None,
                token: Option[String],
                tokenExpirationDate: Option[Date]) {

  def checkPassword(candidate: String): Boolean = PasswordService.checkPassword(candidate, hashPassword.getOrElse(""))

  lazy val isAdministrator: Boolean = roles.contains(Role.administrator)

  lazy val isModerator: Boolean = roles.contains(Role.moderator)

  lazy val isUser: Boolean = roles.contains(Role.user)

}
