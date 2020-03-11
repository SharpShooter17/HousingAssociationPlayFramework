package controllers

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import security.Secured

import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(cc: ControllerComponents)
                              (implicit userDAO: UserDAO, executionContext: ExecutionContext) extends AbstractController(cc) with Secured {

  def users: EssentialAction = isAdministrator { implicit user =>
    implicit request =>
      Ok(views.html.users(userDAO.all()))
  }

}
