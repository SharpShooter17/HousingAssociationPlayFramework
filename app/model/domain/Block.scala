package model.domain

import model.row.{AddressRow, ApartmentRow}

case class Block(id: Long,
                 address: AddressRow,
                 apartments: Set[ApartmentRow] = Set.empty)