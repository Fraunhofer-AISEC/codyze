package de.fraunhofer.aisec.codyze.specification_languages.nwt.dsl

import de.fraunhofer.aisec.codyze.specification_languages.nwt.domain.Account
import de.fraunhofer.aisec.codyze.specification_languages.nwt.domain.Snapshot
import de.fraunhofer.aisec.codyze.specification_languages.nwt.report.Report

class ReportBuilder(val accounts: List<Account>, val snapshots: List<Snapshot>) {
    private val report = Report(accounts, snapshots)

    fun accounts() {
        report.displayAccountList()
    }

    fun snapshots() {
        report.displaySnapshots()
    }
}
