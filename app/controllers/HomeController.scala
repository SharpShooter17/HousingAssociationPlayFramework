package controllers

import dao.UserDAO
import javax.inject._
import play.api.mvc._
import security.Secured
import services.HousingAssociationService

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               service: HousingAssociationService)
                              (implicit userDAO: UserDAO, executionContext: ExecutionContext) extends AbstractController(cc) with Secured {

  def index: EssentialAction = withUser { user =>
    implicit request =>
      val apartmentsAndBills = service.findUserApartments(user)
      val bills = apartmentsAndBills.flatMap(_.bills)
      Ok(views.html.index(user, apartmentsAndBills, bills))
  }

}
