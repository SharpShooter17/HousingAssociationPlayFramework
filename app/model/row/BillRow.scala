package model.row

import java.sql.Date

case class BillRow(id: Long,
                   amount: Double,
                   date: Date,
                   `type`: String,
                   apartmentId: Long)
