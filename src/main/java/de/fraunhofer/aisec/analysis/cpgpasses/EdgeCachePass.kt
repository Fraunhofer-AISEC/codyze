package de.fraunhofer.aisec.analysis.cpgpasses

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.edge.PropertyEdge
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy.AST_FORWARD

enum class EdgeType {
    REFERS_TO,
    AST,
    DFG,
    EOG
}

class Edge(val source: Node, val target: Node, val type: EdgeType)

object Edges {
    private val fromMap: MutableMap<Node, MutableList<Edge>> = HashMap()
    private val toMap: MutableMap<Node, MutableList<Edge>> = HashMap()

    fun add(edge: Edge) {
        if (fromMap[edge.source] == null) {
            fromMap[edge.source] = ArrayList()
        }

        if (toMap[edge.target] == null) {
            toMap[edge.target] = ArrayList()
        }

        fromMap[edge.source]?.add(edge)
        toMap[edge.target]?.add(edge)
    }

    fun to(node: Node, type: EdgeType): List<Edge> {
        return toMap.computeIfAbsent(node) { mutableListOf() }.filter { it.type == type }
    }

    fun from(node: Node, type: EdgeType): List<Edge> {
        return fromMap.computeIfAbsent(node) { mutableListOf() }.filter { it.type == type }
    }

    fun size(): Int {
        return fromMap.size
    }

    fun clear() {
        toMap.clear()
        fromMap.clear()
    }
}

fun Node.followNextEOG(predicate: (PropertyEdge<*>) -> Boolean): List<PropertyEdge<*>>? {
    val path = mutableListOf<PropertyEdge<*>>()

    for (edge in this.nextEOGEdges) {
        val target = edge.end

        path.add(edge)

        if (predicate(edge)) {
            return path
        }

        val subPath = target.followNextEOG(predicate)
        if (subPath != null) {
            path.addAll(subPath)

            return path
        }
    }

    return null
}

class EdgeCachePass : Pass() {
    override fun cleanup() {}

    override fun accept(result: TranslationResult?) {
        Edges.clear()

        if (result != null) {
            for (tu in result.translationUnits) {
                // loop through RecordDeclarations
                tu.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(r: DeclaredReferenceExpression) {
                            r.refersTo?.let {
                                val edge = Edge(r, it, EdgeType.REFERS_TO)
                                Edges.add(edge)
                            }
                        }

                        override fun visit(n: Node?) {
                            if (n != null) {
                                for (node in SubgraphWalker.getAstChildren(n)) {
                                    val edge = Edge(n, node, EdgeType.AST)
                                    Edges.add(edge)
                                }

                                for (dfg in n.prevDFG) {
                                    val edge = Edge(dfg, n, EdgeType.DFG)
                                    Edges.add(edge)
                                }

                                for (dfg in n.nextDFG) {
                                    val edge = Edge(n, dfg, EdgeType.DFG)
                                    Edges.add(edge)
                                }

                                for (eog in n.prevEOG) {
                                    val edge = Edge(eog, n, EdgeType.EOG)
                                    Edges.add(edge)
                                }

                                for (eog in n.nextEOG) {
                                    val edge = Edge(n, eog, EdgeType.EOG)
                                    Edges.add(edge)
                                }
                            }

                            super.visit(n)
                        }
                    }
                )
            }
        }
    }
}

val Node.astParent: Node?
    get() {
        return Edges.to(this, EdgeType.AST).firstOrNull()?.source
    }
