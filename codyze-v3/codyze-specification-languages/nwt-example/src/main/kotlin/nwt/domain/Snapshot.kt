package de.fraunhofer.aisec.codyze.specification_languages.nwt.domain

import java.time.LocalDate

data class Snapshot(val date: LocalDate, val balances: Map<Account, Double>)
