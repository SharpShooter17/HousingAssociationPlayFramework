package controllers

import dao.UserDAO
import javax.inject.{Inject, Singleton}
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

  def blocks: EssentialAction = isAdministrator { implicit admin =>
    implicit request =>
      Ok(views.html.blocks(service.findAllBlocks()))
  }

}
