package services

import java.sql.Date

import dao.{ApartmentDAO, ApartmentOccupantDAO, BillDAO, BlockDAO, UserDAO}
import javax.inject.{Inject, Singleton}
import model.domain.{Apartment, Bill, Block, User}
import model.form.{ApartmentForm, BillForm, BlockForm, UserForm}
import model.row.{AddressRow, ApartmentOccupantRow, ApartmentRow, BillRow, BlockRow, UserRow}

@Singleton
class HousingAssociationService @Inject()(userDAO: UserDAO,
                                          blockDAO: BlockDAO,
                                          apartmentDAO: ApartmentDAO,
                                          billDAO: BillDAO,
                                          apartmentOccupantDAO: ApartmentOccupantDAO) {
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

  def findBlocks(id: Option[Long] = None): Iterable[Block] = blockDAO.find(id)

  def addBlock(blockData: BlockForm): BlockRow = {
    val address = AddressRow(
      zipCode = blockData.zipCode,
      street = blockData.street,
      number = blockData.number,
      city = blockData.city
    )
    blockDAO.insert(address)
  }

  def addApartment(blockId: Long, apartmentData: ApartmentForm) = {
    apartmentDAO.insert(
      ApartmentRow(
        blockId = blockId,
        number = apartmentData.number
      )
    )
  }

  def findApartment(id: Long): Apartment = {
    apartmentDAO.findApartment(id).head
  }

  def addBill(apartmentId: Long, billData: BillForm) = {
    val billRow = BillRow(
      apartmentId = apartmentId,
      date = new Date(billData.date.getTime),
      amount = billData.amount.doubleValue(),
      billType = billData.billType
    )
    billDAO.insert(billRow)
  }

  def addOccupant(apartmentId: Long, userId: Long): Unit = {
    apartmentOccupantDAO.insert(ApartmentOccupantRow(apartmentId, userId))
  }

  def removeOccupant(apartmentId: Long, occupantId: Long) = {
    apartmentOccupantDAO.delete(apartmentId, occupantId)
  }

  def findUserApartments(user: User): Iterable[Apartment] = {
    ???
  }

  def findUserBills(user: User): Iterable[Bill] = {
    ???
  }

}
