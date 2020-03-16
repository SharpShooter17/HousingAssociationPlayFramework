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
class BlockController @Inject()(cc: ControllerComponents,
                                service: HousingAssociationService)
                               (implicit userDAO: UserDAO,
                                executionContext: ExecutionContext)
  extends AbstractController(cc)
    with Secured
    with I18nSupport {

  val blockForm: Form[BlockForm] = Form(mapping(
    "ZipCode" -> text(6, 6),
    "Street" -> text(1),
    "Number" -> text(1),
    "City" -> text(1)
  )(BlockForm.apply)(BlockForm.unapply))

  val apartmentForm: Form[ApartmentForm] = Form(mapping(
    "Number" -> longNumber
  )(ApartmentForm.apply)(ApartmentForm.unapply))

  def blocks: EssentialAction = isModerator { implicit moderator =>
    implicit request =>
      Ok(views.html.blocks(service.findBlocks(), blockForm))
  }

  def addBlock(): EssentialAction = isAdministrator { implicit admin =>
    implicit request =>
      blockForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.blocks(service.findBlocks(), formWithErrors))
        },
        blockData => {
          service.addBlock(blockData)
          Ok(views.html.blocks(service.findBlocks(), blockForm))
        }
      )
  }

  def block(blockId: Long): EssentialAction = isModerator { implicit moderator =>
    implicit request =>
      Ok(views.html.block(service.findBlocks(Some(blockId)).headOption.getOrElse(throw AppException()), apartmentForm))
  }

  def addApartment(blockId: Long): EssentialAction = isAdministrator { implicit admin =>
    implicit request =>
      apartmentForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.block(service.findBlocks(Some(blockId)).headOption.getOrElse(throw AppException()), formWithErrors))
        },
        apartmentData => {
          service.addApartment(blockId, apartmentData)
          Ok(views.html.block(service.findBlocks(Some(blockId)).headOption.getOrElse(throw AppException()), apartmentForm))
        }
      )
  }

}
