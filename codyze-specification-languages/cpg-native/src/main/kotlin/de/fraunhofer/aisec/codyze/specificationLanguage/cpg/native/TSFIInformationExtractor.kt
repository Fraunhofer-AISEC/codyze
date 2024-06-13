package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.declarations.EnumConstantDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.FieldDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Reference
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class TSFIInformationExtractor: InformationExtractor() {

    val securityFunctionMap: MutableMap<Name, SecurityFunction> = mutableMapOf()

    val securityBehavior: MutableSet<String> = mutableSetOf()

    /**
     * Reverse map to more efficiently find SFs related to an appearance of an action.
     */
    val actionsToSFReverseMap: MutableMap<Name,MutableSet<SecurityFunction>> = mutableMapOf()

    override fun extractInformation(result: TranslationResult) {
        val annotatedNode = result.allChildren<Node>({ it.annotations.isNotEmpty()})
        val sfs = annotatedNode.filter { it.annotations.any {it.name.localName == "SF" } }
            .flatMap { it.allChildren<EnumConstantDeclaration>() }
        val securityBs = annotatedNode.filter { it.annotations.any {it.name.lastPartsMatch(Name("TSFI.Behavior")) } }
            .flatMap { it.allChildren<EnumConstantDeclaration>() }
        val sfObjectives = annotatedNode.filter { it.annotations.any { it.name.toString().endsWith("SF.Objectives") } }
        val sfRequirements = annotatedNode.filter { it.annotations.any { it.name.toString().endsWith("SF.Requirements") } }
        val sfActions = annotatedNode.filter { it.annotations.any { it.name.toString().endsWith("SF.SecurityActions") } }
        //val securityBehavior = annotatedNode.filter { it.annotations.any {it.name.endsWith("TSFI.Behavior") } }.flatMap { it.allChildren<EnumConstantDeclaration>() }

        for (sf in sfs){
            if (!securityFunctionMap.contains(sf.name)){
                securityFunctionMap.put(sf.name, SecurityFunction(sf.name,(sf.comment?:"").trimIndent().trim().replace("\n",""),
                    mutableSetOf(), mutableSetOf(), mutableSetOf(), mutableSetOf()))
            }
        }

        for (securityB in securityBs){
            securityBehavior.add(securityB.name.toString())
        }

        // Objectives
        sfObjectives.forEach {
            val sfToObjectives: List<Node> = it.allChildren<Node> { it.astChildren.any { child ->
                securityFunctionMap.keys.any { it.localName == child.name.localName } } }
            sfToObjectives.forEach {parent ->
                parent.astChildren.filter { it is Reference }.firstOrNull()?.let {sf ->
                    val secF = securityFunctionMap.keys.first { it.localName == sf.name.localName }
                    securityFunctionMap[secF]?.objectives?.addAll(parent.allChildren<Literal<String>>().map { it.value?:"" })
                }
            }
        }

        // Requirements
        sfRequirements.forEach {
            val sfToRequirements: List<Node> = it.allChildren<Node> { it.astChildren.any { child ->
                securityFunctionMap.keys.any { it.localName == child.name.localName } } }
            sfToRequirements.forEach {parent ->
                parent.astChildren.filter { it is Reference }.firstOrNull()?.let {sf ->
                    val secF = securityFunctionMap.keys.first { it.localName == sf.name.localName }
                    securityFunctionMap[secF]?.requirements?.addAll(parent.allChildren<Literal<String>>().map { it.value?:"" })
                }
            }
        }


        // Actions
        sfActions.forEach {
            val sfToActions: List<Node> = it.allChildren<Node> { it.astChildren.any { child ->
                securityFunctionMap.keys.any { it.localName == child.name.localName } } }
            sfToActions.forEach {parent ->
                parent.astChildren.filter { it is Reference }.firstOrNull()?.let {sf ->
                    val secF = securityFunctionMap.keys.first { it.localName == sf.name.localName }
                    securityFunctionMap[secF]?.actions?.addAll(parent.allChildren<Literal<String>>().map { Name(it.value?:"") })
                }
            }
        }

        securityFunctionMap.values.forEach { sf ->
            sf.actions.forEach { actionName ->
                if(!actionsToSFReverseMap.contains(actionName)){
                    actionsToSFReverseMap[actionName] = mutableSetOf(sf)
                }else{
                    actionsToSFReverseMap[actionName]?.add(sf)
                }
            }
        }

        val tsfis = annotatedNode.filter { it.annotations.any {it.name.lastPartsMatch(Name("TSFI")) } }

        for (tsfi in tsfis){
            val tsfiSFs: MutableSet<Name> = mutableSetOf()
            var behavior: Name? = null
            val tsfiAnnotation = tsfi.annotations.filter { it.name.lastPartsMatch(Name("TSFI")) }.first()

            val annotationExtendedNode = mutableSetOf(tsfi)
            if(tsfi is FunctionDeclaration){
                tsfi.definition?.let { annotationExtendedNode.add(it) }
            }

            if(tsfi is FieldDeclaration){
                tsfi.definition.let { annotationExtendedNode.add(it) }
            }

            tsfiAnnotation.members.forEach {
                if(it.value is MemberExpression){
                    behavior = Name(it.value.toString())
                }else{
                    tsfiSFs.addAll(it.value.allChildren<MemberExpression>().map { Name(it.toString()) }.toMutableSet())
                }

                if(tsfiSFs.isEmpty()){
                    val calls = annotationExtendedNode.flatMap { reachableCalls(it) }
                    calls.forEach { call ->
                        actionsToSFReverseMap.keys.forEach { actionKey ->
                            if(call.name.toString().startsWith(actionKey.toString())){
                                tsfiSFs.addAll(actionsToSFReverseMap[actionKey]?.map { it.name }?: setOf())
                            }
                        }
                    }
                    if(tsfiSFs.isEmpty()){
                        // TODO Here we could investigate why we do not find the SF for it.
                    }
                }

            }
            val description = annotationExtendedNode.map { it.comment?:"" }.joinToString("\n")
            val tsfiDeclaration = TSFI(description, behavior?:Name(""), annotationExtendedNode)
            tsfiSFs.forEach {
                securityFunctionMap[it]?.tsfis?.add(tsfiDeclaration)
            }
        }

    }

    private fun reachableCalls(start:Node):Set<CallExpression>{
        val reachable = mutableSetOf<Node>(start)
        val worklist = mutableListOf<Node>(start)
        val eogStarters = start.allEOGStarters
        worklist.addAll(eogStarters)
        reachable.addAll(eogStarters)
        while (worklist.isNotEmpty()){
            val current = worklist.removeFirst()
            val nextNodes = mutableSetOf<Node>()
            nextNodes.addAll(current.nextEOG)
            if(current is CallExpression){
                nextNodes.addAll(current.invokes)
            }
            nextNodes.removeIf { reachable.contains(it) }
            worklist.addAll(nextNodes)
            reachable.addAll(nextNodes)
        }
        return reachable.filterIsInstance<CallExpression>().toSet()
    }


    override fun formatInformation(formatter: Formatter): String {
        var xml = ""

        for (sf in securityFunctionMap.values){
            var sfContent = ""
            sfContent += formatter.format("description", sf.description, mapOf())
            sfContent += formatter.format("rational", "", mapOf())

            var content = ""
            for (obj in sf.objectives){
                content += formatter.format("ref","", mapOf("target" to obj))
            }
            sfContent += formatter.format("objectives", content, mapOf())


            content = ""
            for (obj in sf.requirements){
                content += formatter.format("ref","", mapOf("target" to obj))
            }
            sfContent += formatter.format("requirements", content, mapOf())

            xml += formatter.format("security-function", sfContent, mapOf("id" to replaceSFName(sf.name.toString())))
        }


        return prettyPrint(formatter.format("security-specification", xml, mapOf()),2,true)
    }

    private fun replaceSFName(name:String): String{
        var sfname = name.substringAfterLast("de.fraunhofer.aisec.codyze.").replace(".TOEDefinitions.SecurityFunction","")
        return sfname.lowercase()
    }
    private fun prettyPrint(xmlString: String, indent: Int, ignoreDeclaration: Boolean):String{
        try {
            val src = InputSource(StringReader(xmlString));
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);

            val transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            val transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, if(ignoreDeclaration)  "yes" else "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            val out = StringWriter();
            transformer.transform(DOMSource(document), StreamResult(out));
            return out.toString();
        } catch (e:Exception) {
            throw RuntimeException("Error occurs when pretty-printing xml:\n" + xmlString, e);
        }
    }

    /**
     * In contrast to the annotation the data class does not contain the security function.When the TSFIs are parsed, the
     * associated security functions are either provided in the annotation or identified through static code analysis.
     */
    data class TSFI(val description: String, val securityBehavior:Name, val functions:Set<Node>)

    data class SecurityFunction(val name:Name, val description:String, val objectives:MutableSet<String>, val requirements: MutableSet<String>, val actions: MutableSet<Name>, val tsfis:MutableSet<TSFI>)
}