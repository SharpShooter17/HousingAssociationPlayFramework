package model.form

case class UserForm(firstName: String,
                    lastName: String,
                    telephone: String,
                    email: String,
                    roles: Set[String] = Set.empty)
