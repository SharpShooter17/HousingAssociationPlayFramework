package model.domain

import model.row.BillRow

case class Apartment(id: Long,
                     number: Long,
                     occupants: Set[User] = Set.empty,
                     bills: Set[BillRow] = Set.empty)
