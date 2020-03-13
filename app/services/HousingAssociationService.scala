package services

import dao.{BlockDAO, UserDAO}
import javax.inject.{Inject, Singleton}
import model.domain.{Apartment, Bill, Block, User}
import model.form.UserForm

@Singleton
class HousingAssociationService @Inject()(userDAO: UserDAO,
                                          blockDAO: BlockDAO) {
  def findAllBlocks(): Iterable[Block] = {
    blockDAO.find()
  }


  def addUser(form: UserForm): Unit = {
    val user = userDAO.insert(form)
    //TODO Send email to user - set password and activate account
  }

  def findUserApartments(user: User): Iterable[Apartment] = {
    ???
  }

  def findUserBills(user: User): Iterable[Bill] = {
    ???
  }

}
