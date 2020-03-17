package controllers

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import model.form.{LoginForm, UserActivationForm}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request, RequestHeader}
import services.{HousingAssociationService, ReCaptchaService}

@Singleton
class LoginController @Inject()(userDAO: UserDAO,
                                service: HousingAssociationService,
                                reCaptchaService: ReCaptchaService,
                                cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

  val loginForm: Form[LoginForm] = Form(mapping(
    "label.email" -> email,
    "label.password" -> text(8)
  )(LoginForm.apply)(LoginForm.unapply))

  val userActivationForm: Form[UserActivationForm] = Form(mapping(
    "Token" -> nonEmptyText,
    "Password" -> text(8),
    "PasswordConfirmation" -> text(8)
  )(UserActivationForm.apply)(UserActivationForm.unapply))

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

  def activation(token: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.activation(token, userActivationForm.fill(UserActivationForm(token = token, password = "", passwordConfirmation = ""))))
  }

  def activate: Action[AnyContent] = Action { implicit request =>
    request.body.asFormUrlEncoded.get("g-recaptcha-response").headOption match {
      case None => badRequestForActivation
      case Some(recaptcha) =>
        if (!reCaptchaService.verify(recaptcha)) {
          badRequestForActivation
        } else {
          userActivationForm.bindFromRequest.fold(
            formWithErrors => {
              BadRequest(views.html.activation(formWithErrors.data("Token"), formWithErrors))
            },
            userActivationData => {
              service.activate(userActivationData)
              Redirect(routes.LoginController.login())
            }
          )
        }
    }
  }

  private def badRequestForActivation(implicit request: Request[_]) = {
    val form = userActivationForm.bindFromRequest
    BadRequest(views.html.activation(form.data("Token"), form))
  }

}