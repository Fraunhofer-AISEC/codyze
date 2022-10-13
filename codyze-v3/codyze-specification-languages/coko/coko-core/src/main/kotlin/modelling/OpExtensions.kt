package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Nodes
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.callFqn
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.flowsTo
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.signature

context(Project)
/** Get all [Nodes] that are associated with this [Op]. */
fun Op.getAllNodes(): Nodes =
    this@Op.definitions.map { def -> this@Project.callFqn(def.fqn) }.flatten()

context(Project)
/**
 * Get all [Nodes] that are associated with this [Op] and fulfill the [Signature]s of the
 * [Definition]s.
 */
fun Op.getNodes(): Nodes =
    this@Op.definitions
        .map { def ->
            this@Project.callFqn(def.fqn) {
                def.signatures.any { sig ->
                    signature(*sig.parameters.toTypedArray()) &&
                        sig.unorderedParameters.all { it?.flowsTo(arguments) ?: false }
                }
            }
        }
        .flatten()
