package de.fraunhofer.aisec.analysis.structures

import de.fraunhofer.aisec.cpg.graph.Node

class NodeWithValue<T : Node?>(val node: T, var value: ConstantValue) {
    var base: Node? = null

    companion object {
        @JvmStatic
        fun <T : Node?> of(v: NodeWithValue<T>): NodeWithValue<T> {
            val cpgVertexWithValue = NodeWithValue(v.node, v.value)
            cpgVertexWithValue.base = v.base
            return cpgVertexWithValue
        }
    }
}