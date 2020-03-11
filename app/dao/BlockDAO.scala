package dao

import javax.inject.{Inject, Singleton}
import model.row.BlockRow
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BlockDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext) extends Tables with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def all(): Future[Seq[BlockRow]] = db.run(blocks.result)

  def insert(block: BlockRow): Future[Int] = db.run(blocks += block)

}
