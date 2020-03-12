package model.row

import java.sql.Date

case class UserRow(id: Option[Long] = None,
                   email: String,
                   enabled: Boolean = false,
                   firstName: String,
                   password: Option[String] = None,
                   lastName: String,
                   telephone: String,
                   token: Option[String] = None,
                   tokenExpirationDate: Option[Date] = None)
