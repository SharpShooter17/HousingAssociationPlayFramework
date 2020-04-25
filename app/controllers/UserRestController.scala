package controllers

import dao.UserDAO
import exceptions.AppException
import javax.inject.{Inject, Singleton}
import model.domain.User
import model.form.UserForm
import play.api.i18n.I18nSupport
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.HousingAssociationService

import scala.concurrent.ExecutionContext

@Singleton
class UserRestController @Inject()(cc: ControllerComponents, service: HousingAssociationService)
                                  (implicit userDAO: UserDAO, executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  implicit val userWrites = new Writes[User] {
    override def writes(o: User): JsValue = Json.obj(
      "email" -> o.email,
      "name" -> o.firstName,
      "surname" -> o.lastName,
      "telephone" -> o.telephone,
      "enabled" -> o.enabled,
      "roles" -> o.roles
    )
  }

  implicit val userFormReads: Reads[UserForm] = (
    (JsPath \ "firstName").read[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "telephone").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "roles").read[Set[String]]
    ) (UserForm.apply _)

  def users() = Action {
    Ok(Json.arr(userDAO.all().map(u => Json.toJson(u))))
  }

  def addUser() = Action { implicit request =>
    val json = request.body.asJson.get
    json.validate[UserForm] match {
      case s: JsSuccess[UserForm] => service.addUser(s.get)
      case _: JsError => throw AppException()
    }
    Created("User created")
  }

}
