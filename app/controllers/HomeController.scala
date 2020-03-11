package controllers

import javax.inject._
import play.api.mvc._
import security.Secured

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Secured {

  def index  = withUser { user =>
    implicit request =>
      Ok(views.html.index(""))
  }

}
