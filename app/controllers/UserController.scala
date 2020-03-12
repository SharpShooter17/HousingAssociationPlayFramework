package controllers

import dao.UserDAO
import exceptions.AppException
import javax.inject.{Inject, Singleton}
import model.form.UserForm
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import security.Secured

import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(cc: ControllerComponents)
                              (implicit userDAO: UserDAO, executionContext: ExecutionContext)
  extends AbstractController(cc)
    with I18nSupport
    with Secured {

  val userForm: Form[UserForm] = Form(mapping(
    "Name" -> text,
    "Surname" -> text,
    "Telephone" -> text,
    "Email" -> email,
    "Roles" -> set(text))(UserForm.apply)(UserForm.unapply))

  def users = isAdministrator { implicit admin =>
    implicit request =>
      Ok(views.html.users(userDAO.all(), userForm))
  }

  def addUser: EssentialAction = isAdministrator { implicit admin =>
    implicit request =>
      val postVals = request.body.asFormUrlEncoded
      val form = postVals.map { args =>
        val isAdministrator = args.getOrElse("isAdministrator", Seq.empty).headOption.getOrElse("false").toBoolean
        val isModerator = args.getOrElse("isModerator", Seq.empty).headOption.getOrElse("false").toBoolean
        val isUser = args.getOrElse("isUser", Seq.empty).headOption.getOrElse("false").toBoolean
        val roles: Set[String] = setIfTrue(isAdministrator, "ADMINISTRATOR") ++
          setIfTrue(isModerator, "MODERATOR") ++
          setIfTrue(isUser, "USER")

        UserForm(
          firstName = args.getOrElse("Name", Seq.empty).headOption.getOrElse(""),
          lastName = args.getOrElse("Surname", Seq.empty).headOption.getOrElse(""),
          telephone = args.getOrElse("Telephone", Seq.empty).headOption.getOrElse(""),
          email = args.getOrElse("Email", Seq.empty).headOption.getOrElse(""),
          roles = roles
        )
      }.getOrElse(throw AppException())

      userDAO.insert(form)
      //TODO Send email to user - set password and activate account
      Ok(views.html.users(userDAO.all(), userForm))
  }

  private def setIfTrue(expression: Boolean, item: String): Set[String] = {
    if (expression) Set(item) else Set.empty
  }

}