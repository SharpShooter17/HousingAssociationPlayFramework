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
                hashPassword: String,
                token: Option[String],
                tokenExpirationDate: Option[Date]) {

  def checkPassword(candidate: String): Boolean = PasswordService.checkPassword(candidate, hashPassword)

}
