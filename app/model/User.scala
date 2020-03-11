package model

import java.sql.Date

case class User(id: Long,
                email: String,
                enabled: Boolean,
                firstName: String,
                password: String,
                lastName: String,
                telephone: String,
                token: Option[String],
                tokenExpirationDate: Option[Date])
