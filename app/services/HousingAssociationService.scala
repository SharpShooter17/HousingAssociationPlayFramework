package services

import java.io.ByteArrayOutputStream
import java.sql.Date
import java.time.LocalDate
import java.util.UUID

import dao._
import exceptions.AppException
import javax.inject.{Inject, Singleton}
import model.domain.{Apartment, Block, User}
import model.form._
import model.row._
import security.PasswordService

@Singleton
class HousingAssociationService @Inject()(userDAO: UserDAO,
                                          blockDAO: BlockDAO,
                                          apartmentDAO: ApartmentDAO,
                                          billDAO: BillDAO,
                                          apartmentOccupantDAO: ApartmentOccupantDAO,
                                          mailSenderService: MailSenderService,
                                          pdfService: PdfService) {

  private val daysToTokenExpiration = 7

  def addUser(form: UserForm): Unit = {
    val userRow = UserRow(
      email = form.email.toLowerCase,
      firstName = form.firstName,
      lastName = form.lastName,
      telephone = form.telephone,
      token = Some(UUID.randomUUID.toString),
      tokenExpirationDate = Some(new Date(LocalDate.now.plusDays(daysToTokenExpiration).toEpochDay * 24 * 60 * 60 * 1000))
    )
    val user = userDAO.insert(userRow, form.roles)
    mailSenderService.sendEmail(user)
  }

  def findBlocks(id: Option[Long] = None): Iterable[Block] = blockDAO.find(id)


  def addBlock(blockData: BlockForm): Unit = {
    val address = AddressRow(
      zipCode = blockData.zipCode,
      street = blockData.street,
      number = blockData.number,
      city = blockData.city
    )
    blockDAO.insert(address)
  }

  def addApartment(blockId: Long, apartmentData: ApartmentForm): Unit = {
    apartmentDAO.insert(
      ApartmentRow(
        blockId = blockId,
        number = apartmentData.number
      )
    )
  }

  def findApartment(id: Long): Apartment = {
    apartmentDAO.findApartments(Some(id)).head
  }

  def addBill(apartmentId: Long, billData: BillForm): Unit = {
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

  def removeOccupant(apartmentId: Long, occupantId: Long): Unit = {
    apartmentOccupantDAO.delete(apartmentId, occupantId)
  }

  def findUserApartments(user: User): Iterable[Apartment] = {
    apartmentDAO.findApartmentsByUserId(user.id)
  }

  private def findUserByToken(token: String): User = {
    val user = userDAO.findByToken(token).getOrElse(throw AppException())
    val currentDate = new Date(new java.util.Date().getTime)
    if (user.tokenExpirationDate.exists(_.before(currentDate))) {
      throw AppException()
    }
    user
  }

  def activate(data: UserActivationForm): Unit = {
    val user = findUserByToken(data.token)
    if (data.password == data.passwordConfirmation) {
      val hashedPassword = PasswordService.createPassword(data.password)
      val userWithPassword = user.copy(
        hashPassword = Some(hashedPassword),
        enabled = true,
        token = None,
        tokenExpirationDate = None
      )
      userDAO.update(userWithPassword)
    }
  }

  def downloadBillPdf(billId: Long): ByteArrayOutputStream = {
    val bill = billDAO.find(billId).getOrElse(throw AppException())
    pdfService.createPdf(bill)
  }

  def allApartments(): Iterable[Apartment] = {
    apartmentDAO.findApartments()
  }

}
