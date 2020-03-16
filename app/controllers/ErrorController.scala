package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class ErrorController @Inject()(cc: ControllerComponents)
                               (implicit executionContext: ExecutionContext) extends AbstractController(cc) {

  def accessDenied: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.error("Access Denied - 403", "You have no permission to visit this page!"))
  }
}
