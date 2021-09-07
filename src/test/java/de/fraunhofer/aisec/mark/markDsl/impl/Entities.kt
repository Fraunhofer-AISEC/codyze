package de.fraunhofer.aisec.mark.markDsl.impl

import de.fraunhofer.aisec.mark.markDsl.EntityDeclaration
import de.fraunhofer.aisec.mark.markDsl.MarkModel

fun MarkModel.entity(name: String, init: EntityDeclaration.() -> Unit): EntityDeclaration {
    val declaration = EntityDeclarationImpl()
    declaration.name = name
    declaration.init()
    this.decl += declaration
    return declaration
}
