package controllers

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import model.form.UserForm
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import security.Secured

import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(cc: ControllerComponents)
                              (implicit userDAO: UserDAO, executionContext: ExecutionContext) extends AbstractController(cc) with Secured {

  val userForm: Form[UserForm] = Form(mapping(
    "Name" -> text,
    "Surname" -> text,
    "Telephone" -> text,
    "Email" -> email,
    "Roles" -> set(text),
    "Password" -> text(8))(UserForm.apply)(UserForm.unapply))

  def users: EssentialAction = isAdministrator { implicit user =>
    implicit request =>
      Ok(views.html.users(userDAO.all(), userForm))
  }

  def addUser = isAdministrator { implicit user =>
    implicit request =>
      val postVals = request.body.asFormUrlEncoded
      postVals.map { args =>
        val emailValue = args.getOrElse("Email", Seq.empty).headOption.getOrElse("")
        userDAO.insert(UserForm(
          // TODO
        ))
      }
      users()
  }

}