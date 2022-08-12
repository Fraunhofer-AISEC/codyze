package de.fraunhofer.aisec.codyze.specification_languages.nwt.report

import de.fraunhofer.aisec.codyze.specification_languages.nwt.domain.Account
import de.fraunhofer.aisec.codyze.specification_languages.nwt.domain.Snapshot

class Report(val accounts: List<Account>, val snapshots: List<Snapshot>) {
    fun displayAccountList() {
        println("ACCOUNTS:")
        accounts.forEach { println("- ${it.name}") }
    }

    fun displaySnapshots() {
        println("SNAPSHOTS:")
        snapshots.forEach { snapshot ->
            println("* ${snapshot.date}:")
            accounts.forEach { account ->
                println("  - ${account.name}: ${snapshot.balances[account]?.toString() ?: "-"}")
            }
        }
    }
}
