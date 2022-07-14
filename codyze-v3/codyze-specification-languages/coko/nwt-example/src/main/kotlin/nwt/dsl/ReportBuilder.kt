package de.fraunhofer.aisec.codyze.specification_languages.coko.nwt.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.nwt.domain.Account
import de.fraunhofer.aisec.codyze.specification_languages.coko.nwt.domain.Snapshot
import de.fraunhofer.aisec.codyze.specification_languages.coko.nwt.report.Report

class ReportBuilder(val accounts: List<Account>, val snapshots: List<Snapshot>) {
    private val report = Report(accounts, snapshots)

    fun accounts() {
        report.displayAccountList()
    }

    fun snapshots() {
        report.displaySnapshots()
    }
}
