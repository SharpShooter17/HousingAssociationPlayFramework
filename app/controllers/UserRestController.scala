package controllers

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import model.domain.User
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction, Result}
import services.HousingAssociationService

import scala.concurrent.ExecutionContext

@Singleton
class UserRestController @Inject()(cc: ControllerComponents, service: HousingAssociationService)
                                  (implicit userDAO: UserDAO, executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  implicit def userWrites = new Writes[User] {
    override def writes(o: User): JsValue = Json.obj(
      "email" -> o.email,
      "name" -> o.firstName,
      "surname" -> o.lastName,
      "telephone" -> o.telephone,
      "enabled" -> o.enabled,
      "roles" -> o.roles
    )
  }

  def users() = Action {
    Ok(Json.arr(userDAO.all().map(u => Json.toJson(u))))
  }

}
