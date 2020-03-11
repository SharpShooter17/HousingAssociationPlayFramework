package controllers

import dao.UserDAO
import javax.inject._
import play.api.mvc._
import security.Secured

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject()(cc: ControllerComponents)
                              (implicit userDAO: UserDAO, executionContext: ExecutionContext) extends AbstractController(cc) with Secured {

  def index: EssentialAction = withUser { user =>
    implicit request =>
      Ok(views.html.index(user))
  }

}
