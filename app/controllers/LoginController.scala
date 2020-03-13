package controllers

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import model.form.LoginForm
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}

@Singleton
class LoginController @Inject()(userDAO: UserDAO,
                                cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  private val password = "Password"

  val loginForm: Form[LoginForm] = Form(mapping(
    "Email" -> email,
    password -> text(8)
  )(LoginForm.apply)(LoginForm.unapply))

  def login: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def validateLogin: Action[AnyContent] = Action { implicit request =>

    loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.login(formWithErrors))
      },
      loginData => {
        userDAO.findByEmail(loginData.email) match {
          case Some(user) =>
            if (user.checkPassword(loginData.password)) {
              Redirect(routes.HomeController.index()).withSession(
                "email" -> user.email,
                "id" -> user.id.toString
              )
            } else {
              Redirect(routes.LoginController.login()).flashing("error" -> "Invalid email or password")
            }
          case None => Redirect(routes.LoginController.login()).flashing("error" -> "User with given email does not exists")
        }
      }
    )
  }

  def logout: Action[AnyContent] = Action {
    Redirect(routes.LoginController.login()).withNewSession
  }

}