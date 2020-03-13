package model.row

import java.sql.Date

case class BillRow(id: Option[Long] = None,
                   amount: Double,
                   date: Date,
                   billType: String,
                   apartmentId: Long)
