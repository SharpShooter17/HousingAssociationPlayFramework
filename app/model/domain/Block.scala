package model.domain

import model.row.AddressRow

case class Block(id: Long,
                 address: AddressRow,
                 apartments: Set[Apartment] = Set.empty)