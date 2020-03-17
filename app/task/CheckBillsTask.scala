package task

import java.sql.Date
import java.time.Instant

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import model.domain.Apartment
import model.form.BillForm
import services.HousingAssociationService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class CheckBillsTask @Inject()(actorSystem: ActorSystem, service: HousingAssociationService)
                              (implicit executionContext: ExecutionContext) {

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = 10.seconds, interval = 1.minute) { () =>
    actorSystem.log.info("Check bills job start")

    val apartments = service.allApartments()
    apartments.foreach(addBills)

    actorSystem.log.info("Check bills job end")
  }

  private def addBills(apartment: Apartment): Unit = {
    service.addBill(
      apartmentId = apartment.id,
      billData = BillForm(
        amount = BigDecimal(10.0),
        date = new Date(Instant.now().getEpochSecond * 1000),
        billType = "GAS"
      )
    )
  }

}
