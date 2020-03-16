package controllers

import java.io.{ByteArrayOutputStream, File, FileOutputStream}

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import model.form.{BillForm, OccupantForm}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, EssentialAction}
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

  val occupantForm: Form[OccupantForm] = Form(mapping(
    "Id" -> nonEmptyText
  )(OccupantForm.apply)(OccupantForm.unapply))

  def apartment(id: Long): EssentialAction = isModerator { implicit moderator =>
    implicit request =>
      Ok(views.html.apartment(service.findApartment(id), billForm, occupantForm, occupantList))
  }

  def addBill(apartmentId: Long): EssentialAction = isModerator { implicit moderator =>
    implicit request =>
      billForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.apartment(service.findApartment(apartmentId), formWithErrors, occupantForm, occupantList))
        },
        billData => {
          service.addBill(apartmentId, billData)
          Ok(views.html.apartment(service.findApartment(apartmentId), billForm, occupantForm, occupantList))
        }
      )
  }

  def addOccupant(apartmentId: Long): EssentialAction = isModerator { implicit moederator =>
    implicit request =>
      occupantForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.apartment(service.findApartment(apartmentId), billForm, formWithErrors, occupantList))
        },
        occupantData => {
          service.addOccupant(apartmentId, occupantData.id.toLong)
          Ok(views.html.apartment(service.findApartment(apartmentId), billForm, occupantForm, occupantList))
        }
      )
  }

  def removeOccupant(apartmentId: Long, occupantId: Long): EssentialAction = isModerator { implicit moderator =>
    implicit request =>
      service.removeOccupant(apartmentId, occupantId)
      Ok(views.html.apartment(service.findApartment(apartmentId), billForm, occupantForm, occupantList))
  }

  private def occupantList: Seq[(String, String)] = {
    userDAO.all().filter(_.isUser).map(user => (user.id.toString, user.email)).toSeq
  }

  def downloadBillInPdf(billId: Long): Action[AnyContent] = Action { implicit request =>
    val outputStream: ByteArrayOutputStream = service.downloadBillPdf(billId)
    val fileName = s"Bill_$billId.pdf"
    val fos = new FileOutputStream(fileName)
    fos.write(outputStream.toByteArray)
    fos.close()

    Ok.sendFile(new File(fileName), inline = true)
  }

}
