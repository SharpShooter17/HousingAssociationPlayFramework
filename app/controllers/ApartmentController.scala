package controllers

import dao.UserDAO
import exceptions.AppException
import javax.inject.{Inject, Singleton}
import model.form.{ApartmentForm, BlockForm}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents, EssentialAction}
import security.Secured
import services.HousingAssociationService

import scala.concurrent.ExecutionContext

@Singleton
class ApartmentController @Inject()(cc: ControllerComponents,
                                    service: HousingAssociationService)
                                   (implicit userDAO: UserDAO,
                                    executionContext: ExecutionContext)
  extends AbstractController(cc)
    with Secured
    with I18nSupport {

  def apartment(id: Long): EssentialAction = isAdministrator { implicit admin =>
    implicit request =>
      Ok(views.html.apartment(service.findApartment(id)))
  }

}
