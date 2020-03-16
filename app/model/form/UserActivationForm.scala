package model.form

case class UserActivationForm(token: String,
                              password: String,
                              passwordConfirmation: String)
