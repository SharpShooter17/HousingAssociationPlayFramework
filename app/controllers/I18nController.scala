package controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

@Singleton
class I18nController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

  def english: Action[AnyContent] = Action {
    Redirect("/homepage").withLang(Lang("en"))
  }

  def polish: Action[AnyContent] = Action {
    Redirect("/homepage").withLang(Lang("pl"))
  }

}
