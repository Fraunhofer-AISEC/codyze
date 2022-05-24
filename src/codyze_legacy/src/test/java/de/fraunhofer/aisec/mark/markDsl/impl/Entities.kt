package de.fraunhofer.aisec.mark.markDsl.impl

import de.fraunhofer.aisec.mark.markDsl.*

fun MarkModel.entity(name: String, init: EntityDeclaration.() -> Unit): EntityDeclaration {
    val declaration = EntityDeclarationImpl()
    declaration.name = name
    declaration.init()
    this.decl += declaration
    return declaration
}

fun EntityDeclaration.op(name: String, init: OpDeclaration.() -> Unit): OpDeclaration {
    val declaration = OpDeclarationImpl()
    declaration.name = name
    declaration.init()
    this.content += declaration
    return declaration
}

fun EntityDeclaration.variable(
    name: String,
    type: String? = null,
    init: VariableDeclaration.() -> Unit = {}
): VariableDeclaration {
    val declaration = VariableDeclarationImpl()
    declaration.name = name
    type?.let { declaration.type = it }
    declaration.init()
    this.content += declaration
    return declaration
}

fun OpDeclaration.stmt(`var`: String? = null, init: OpStatement.() -> Unit = {}): OpStatement {
    val stmt = OpStatementImpl()
    stmt.`var` = `var`
    stmt.init()
    this.stmts += stmt
    return stmt
}

fun OpStatement.call(name: String, init: FunctionDeclaration.() -> Unit = {}): FunctionDeclaration {
    val declaration = FunctionDeclarationImpl()
    declaration.name = name
    declaration.init()
    this.call = declaration
    return declaration
}

fun FunctionDeclaration.param(
    name: String? = null,
    type: String? = null,
    init: Parameter.() -> Unit = {}
): Parameter {
    val param = ParameterImpl()
    name?.let { param.`var` = name }
    type?.let { param.types += it }
    param.init()
    this.params += param
    return param
}
