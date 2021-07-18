package de.fraunhofer.aisec.codyze.analysis.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.Pass

class IdentifierPass : Pass() {
    companion object {
        var lastId: Long = 0
    }

    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            handle(tu)
        }
    }

    private fun handle(node: Node) {
        node.id = lastId++

        for (child in SubgraphWalker.getAstChildren(node)) {
            handle(child)
        }
    }

    override fun cleanup() {
        // nothing to do
    }
}
