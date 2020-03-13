package dao

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import model.domain.Apartment
import model.row.{ApartmentOccupantRow, ApartmentRow, BillRow, UserRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class ApartmentDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                            (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private def insertQuery() = apartments returning apartments.map(_.id) into ((address, id) => address.copy(id = id))

  def insert(row: ApartmentRow): ApartmentRow = {
    val apartmentWithId = insertQuery() += row
    val future = db.run(apartmentWithId)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

  def findApartment(id: Long): Iterable[Apartment] = {
    val future = db.run(getQuery(Some(id)).result).map(mapApartment)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

  def findApartmentsByUserId(userId: Long) : Iterable[Apartment] = {
    val filteredQuery = getQuery(userId = Some(userId))
    val future = db.run(filteredQuery.result).map(mapApartment)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

  private def getQuery(maybeId: Option[Long] = None, userId: Option[Long] = None) = {
    val query = maybeId match {
      case None => apartments
      case Some(id) => apartments.filter(_.id === id)
    }

    val withOccupants = joinOccupants(query, userId)
    joinBills(withOccupants)
  }

  private def joinOccupants(query: Query[ApartmentTable, ApartmentRow, Seq], userId: Option[Long] = None) = {
    val apartmentOccupants: Query[(ApartmentTable, Rep[Option[ApartmentOccupantTable]]), (ApartmentRow, Option[ApartmentOccupantRow]), Seq] = for {
      (apartment, apartmentOccupants) <- query.joinLeft(apartmentsOccupants).on((a, ao) => a.id === ao.apartmentId)
    } yield (apartment, apartmentOccupants)

    val withFilter = userId match {
      case None => apartmentOccupants
      case Some(id) => apartmentOccupants.filter((_._2.map(_.occupantId === id)))
    }

    for {
      (apartmentOccupants, occupants) <- withFilter.joinLeft(users)
        .on((apartment, o) => apartment._2.map(apartmentOccupant => apartmentOccupant.occupantId) === o.id)
    } yield (apartmentOccupants, occupants)
  }

  private def joinBills(withOccupants: Query[((ApartmentTable, Rep[Option[ApartmentOccupantTable]]), Rep[Option[UserTable]]), ((ApartmentRow, Option[ApartmentOccupantRow]), Option[UserRow]), Seq]) = {
    for {
      (apartment, bills) <- withOccupants.joinLeft(bills).on((apartment, bill) => apartment._1._1.id === bill.apartmentId)
    } yield (apartment, bills)
  }

  private def mapApartment(items: Seq[(((ApartmentRow, Option[ApartmentOccupantRow]), Option[UserRow]), Option[BillRow])]): Iterable[Apartment] = {
    case class MappedItems(apartment: ApartmentRow, userRow: Option[UserRow], bill: Option[BillRow])

    val mappedItems = items.map(item =>
      MappedItems(
        apartment = item._1._1._1,
        userRow = item._1._2,
        bill = item._2
      ))

    val byApartment: Map[ApartmentRow, Seq[MappedItems]] = mappedItems.groupBy(_.apartment)

    byApartment.map{ case (apartmentRow, items) =>
      Apartment(
        id = apartmentRow.id.getOrElse(-1L),
        number = apartmentRow.number,
        blockId = apartmentRow.blockId,
        occupants = items.flatMap(_.userRow).toSet,
        bills = items.flatMap(_.bill).toSet
      )
    }
  }

}
