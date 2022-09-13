package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import de.fraunhofer.aisec.cpg.TranslationResult

@Suppress("UNUSED")
/*
 * Receives a [cpg] translation result to identify matching nodes and evaluate the expressions.
 * All the functions of the DSL are implemented as extension functions of [Project].
 */
class Project(val cpg: TranslationResult)
