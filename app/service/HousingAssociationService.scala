package service

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import model.form.UserForm

@Singleton
class HousingAssociationService @Inject()(userDAO: UserDAO) {

  def addUser(form: UserForm): Unit = {
    val user = userDAO.insert(form)
    //TODO Send email to user - set password and activate account
  }

}
