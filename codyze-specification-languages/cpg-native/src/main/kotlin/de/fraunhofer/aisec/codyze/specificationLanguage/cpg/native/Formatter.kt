package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

open abstract class Formatter {
    public abstract fun format(k: String, v: String, attributes: Map<String, String>): String
}
