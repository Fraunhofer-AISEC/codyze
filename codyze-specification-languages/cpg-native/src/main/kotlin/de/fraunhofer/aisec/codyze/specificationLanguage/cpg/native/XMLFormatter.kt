package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

class XMLFormatter: Formatter() {
    override fun format(k: String, v: String): String {
        return "<" + k + ">" + v + "</" + k + ">" // TODO Replace this with an xml output generator that has some injection mitigations
    }
}