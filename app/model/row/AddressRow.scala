package model.row

case class AddressRow(id: Option[Long] = None,
                      city: String = "",
                      number: String = "",
                      street: String = "",
                      zipCode: String = "")
