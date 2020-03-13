package services

import dao.{BlockDAO, UserDAO}
import javax.inject.{Inject, Singleton}
import model.domain.{Apartment, Bill, Block, User}
import model.form.{BlockForm, UserForm}
import model.row.{AddressRow, BlockRow, UserRow}

@Singleton
class HousingAssociationService @Inject()(userDAO: UserDAO,
                                          blockDAO: BlockDAO) {
  def addUser(form: UserForm): Unit = {
    val userRow = UserRow(
      email = form.email.toLowerCase,
      firstName = form.firstName,
      lastName = form.lastName,
      telephone = form.telephone
    )
    val user = userDAO.insert(userRow, form.roles)
    //TODO Send email to user - set password and activate account
  }

  def findAllBlocks(): Iterable[Block] = blockDAO.find()


  def addBlock(blockData: BlockForm): BlockRow = {
    val address = AddressRow(
      zipCode = blockData.zipCode,
      street = blockData.street,
      number = blockData.number,
      city = blockData.city
    )
    blockDAO.insert(address)
  }

  def findUserApartments(user: User): Iterable[Apartment] = {
    ???
  }

  def findUserBills(user: User): Iterable[Bill] = {
    ???
  }

}
