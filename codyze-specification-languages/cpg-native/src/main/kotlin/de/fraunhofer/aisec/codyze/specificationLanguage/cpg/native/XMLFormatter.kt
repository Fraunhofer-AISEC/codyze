package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import org.koin.core.definition.indexKey

class XMLFormatter: Formatter() {
    override fun format(k: String, v: String, attributes: Map<String,String>): String {
        var output = "<" + k
        for(att in attributes.keys){
            output += " " + att + "=\"" + attributes[att] + "\""
        }
        if(v.isNotEmpty()){
            output += ">" + v + "</" + k +">"
        }else{
            output += " />"
        }


        return output // TODO Replace this with an xml output generator that has some injection mitigations
    }
}