package services

import javax.inject.{Inject, Singleton}
import play.api.i18n.{Lang, Langs}

@Singleton
class I18nService @Inject()(langs: Langs) {

  val availableLangs: Seq[Lang] = langs.availables

}
