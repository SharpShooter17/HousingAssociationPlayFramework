package dao

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import model.domain.{Apartment, Block}
import model.row.{AddressRow, ApartmentRow, BlockRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class BlockDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, addressDAO: AddressDAO)
                        (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def find(id: Option[Long] = None): Iterable[Block] = {
    val future = db.run(getBlockQuery(id).result).map(mapBlock)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

  private def insertQuery() = blocks returning blocks.map(_.id) into ((block, id) => block.copy(id = id))

  def insert(address: AddressRow): BlockRow = {
    val addressRow = addressDAO.insert(address)
    val blockWithId = insertQuery() += BlockRow(addressId = addressRow.id.getOrElse(-1L))
    val future = db.run(blockWithId)
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

  private def getBlockQuery(maybeId: Option[Long] = None) = {
    val query = maybeId match {
      case None => blocks
      case Some(id) => blocks.filter(_.id === id)
    }
    val withApartments = joinApartments(query)
    joinAddress(withApartments)
  }

  private def joinApartments(query: Query[BlockTable, BlockRow, Seq]) = {
    for {
      (block, apartment) <- query.joinLeft(apartments).on((b, a) => b.id === a.blockId)
    } yield (block, apartment)
  }

  private def joinAddress(query: Query[(BlockTable, Rep[Option[ApartmentTable]]), (BlockRow, Option[ApartmentRow]), Seq]) = {
    for {
      (block, address) <- query.join(addresses).on((b, a) => b._1.addressId === a.id)
    } yield (block, address)
  }

  private def mapBlock(blocks: Seq[((BlockRow, Option[ApartmentRow]), AddressRow)]): Iterable[Block] = {
    case class BlockWithApartmentsAndAddress(block: BlockRow, apartments: Iterable[ApartmentRow], address: AddressRow)

    val mappedBlocks = blocks.map(item => (item._1._1, item._1._2, item._2))

    val byBlock = mappedBlocks.groupBy {
      case (block, _, _) => block
    }

    val blockWithApartmentsAndAddress = byBlock.map { case (block, blockWithApartmentsAndAddress) =>
      BlockWithApartmentsAndAddress(block,
        blockWithApartmentsAndAddress.flatMap { case (_, optApartment, _) =>
          optApartment
        },
        blockWithApartmentsAndAddress.map { case (_, _, address) => address }.headOption.getOrElse(AddressRow())
      )
    }

    blockWithApartmentsAndAddress.map(item =>
      Block(
        id = item.block.id.getOrElse(-1L),
        address = item.address,
        apartments = item.apartments.map(mapApartmentRow).toSet
      )
    )
  }

  private def mapApartmentRow(row: ApartmentRow) = Apartment(
    id = row.id.getOrElse(-1L),
    number = row.number,
    blockId = row.blockId
  )

}
