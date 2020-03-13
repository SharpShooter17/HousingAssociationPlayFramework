package controllers

import dao.UserDAO
import exceptions.AppException
import javax.inject.{Inject, Singleton}
import model.domain.Role
import model.form.UserForm
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import security.Secured
import services.HousingAssociationService

import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(cc: ControllerComponents, service: HousingAssociationService)
                              (implicit userDAO: UserDAO, executionContext: ExecutionContext)
  extends AbstractController(cc)
    with I18nSupport
    with Secured {

  val userForm: Form[UserForm] = Form(mapping(
    "Name" -> nonEmptyText,
    "Surname" -> nonEmptyText,
    "Telephone" -> nonEmptyText,
    "Email" -> email,
    "Roles" -> set(text))(UserForm.apply)(UserForm.unapply))

  def users: EssentialAction = isAdministrator { implicit admin =>
    implicit request =>
      Ok(views.html.users(userDAO.all(), userForm))
  }

  def addUser: EssentialAction = isAdministrator { implicit admin =>
    implicit request =>
      val postVals = request.body.asFormUrlEncoded
      val roles = postVals.map { args =>
        val isAdministrator = args.getOrElse("isAdministrator", Seq.empty).headOption.getOrElse("false").toBoolean
        val isModerator = args.getOrElse("isModerator", Seq.empty).headOption.getOrElse("false").toBoolean
        val isUser = args.getOrElse("isUser", Seq.empty).headOption.getOrElse("false").toBoolean
        setIfTrue(isAdministrator, Role.administrator) ++ setIfTrue(isModerator, Role.moderator) ++ setIfTrue(isUser, Role.user)
      }.getOrElse(throw AppException())

      userForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.users(userDAO.all(), formWithErrors))
        },
        userData => {
          service.addUser(userData.copy(roles = roles))
          Ok(views.html.users(userDAO.all(), userForm))
        }
      )
  }

  private def setIfTrue(expression: Boolean, item: String): Set[String] = {
    if (expression) Set(item) else Set.empty
  }

}