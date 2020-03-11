package controllers

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import security.PasswordService

@Singleton
class LoginController @Inject()(userDAO: UserDAO,
                                cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  private val email = "Email"
  private val password = "Password"

  val loginForm: Form[LoginForm] = Form(mapping(
    email -> text(3, 255),
    password -> text(8)
  )(LoginForm.apply)(LoginForm.unapply))

  def login: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def validateLogin: Action[AnyContent] = Action { implicit request =>
    val postVals: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded
    postVals.map { args =>
      val emailValue = args.getOrElse(email, Seq.empty).headOption.getOrElse("")
      val passwordValue = args.getOrElse(password, Seq.empty).headOption.getOrElse("")
      userDAO.findByEmail(emailValue) match {
        case Some(user) =>
          if (PasswordService.checkPassword(passwordValue, user.password)) {
            Redirect(routes.HomeController.index()).withSession(
              "email" -> user.email,
              "id" -> user.id.toString
            )
          } else {
            Redirect(routes.LoginController.login()).flashing("error" -> "Invalid email or password")
          }
        case None => Redirect(routes.LoginController.login()).flashing("error" -> "User with given email does not exists")
      }
    }.getOrElse(Redirect(routes.LoginController.login()))
  }

  def logout: Action[AnyContent] = Action {
    Redirect(routes.LoginController.login()).withNewSession
  }

}

case class LoginForm(email: String, password: String)