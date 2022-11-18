package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

sealed interface OrderNode : OrderFragment {
    /** Convert this [OrderNode] to a binary syntax tree */
    override fun toNode() = this

    /**
     * Apply the given [block] to each [OrderNode] in this [OrderNode].
     * Uses Depth First Search (DFS).
     */
    fun applyToAll(block: OrderNode.() -> Unit) {
        val stack = ArrayDeque<OrderNode>()
        stack.addLast(this)
        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            when (node) {
                is TerminalOrderNode -> {}
                is SequenceOrderNode -> {
                    stack.addLast(node.left)
                    stack.addLast(node.right)
                }
                is AlternativeOrderNode -> {
                    stack.addLast(node.left)
                    stack.addLast(node.right)
                }
                is QuantifierOrderNode -> stack.addLast(node.child)
            }
            node.apply(block)
        }
    }
}
