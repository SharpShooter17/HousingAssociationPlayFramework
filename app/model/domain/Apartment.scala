package model.domain

import model.row.{BillRow, UserRow}

case class Apartment(id: Long,
                     number: Long,
                     blockId: Long,
                     occupants: Set[UserRow] = Set.empty,
                     bills: Set[BillRow] = Set.empty)
