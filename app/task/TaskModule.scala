package task

import play.api.inject.SimpleModule
import play.api.inject._

class TaskModule extends SimpleModule(bind[CheckBillsTask].toSelf.eagerly()) {

}
