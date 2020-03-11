package model.row

import java.sql.Date

case class UserRow(id: Long,
                   email: String,
                   enabled: Boolean,
                   firstName: String,
                   password: String,
                   lastName: String,
                   telephone: String,
                   token: Option[String],
                   tokenExpirationDate: Option[Date])
