package model.form

import java.util.Date

case class BillForm(amount: BigDecimal,
                    date: Date,
                    billType: String)
