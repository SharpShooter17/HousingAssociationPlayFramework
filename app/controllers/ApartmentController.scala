package controllers

import dao.UserDAO
import exceptions.AppException
import javax.inject.{Inject, Singleton}
import model.form.BillForm
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

  val billForm: Form[BillForm] = Form(mapping(
    "Amount" -> bigDecimal(10, 2),
    "Date" -> date,
    "Type" -> nonEmptyText
  )(BillForm.apply)(BillForm.unapply))

  def apartment(id: Long): EssentialAction = isAdministrator { implicit admin =>
    implicit request =>
      Ok(views.html.apartment(service.findApartment(id), billForm))
  }

  def addBill(apartmentId: Long) = isModerator { implicit moderator => implicit request =>
    billForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.apartment(service.findApartment(apartmentId), formWithErrors))
      },
      billData => {
        service.addBill(apartmentId, billData)
        Ok(views.html.apartment(service.findApartment(apartmentId), billForm))
      }
    )
  }

}
