/*
 * Copyright (c) 2024, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.declarations.EnumConstantDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.FieldDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.graph.types.Type
import io.github.oshai.kotlinlogging.KotlinLogging
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class TSFIInformationExtractor : InformationExtractor() {

    private val logger = KotlinLogging.logger {}

    val securityFunctionMap: MutableMap<Name, SecurityFunction> = mutableMapOf()

    val securityBehavior: MutableSet<String> = mutableSetOf()

    val tsfiDeclarations: MutableList<TSFI> = mutableListOf<TSFI>()

    /**
     * Reverse map to more efficiently find SFs related to an appearance of an action.
     */
    val actionsToSFReverseMap: MutableMap<Name, MutableSet<SecurityFunction>> = mutableMapOf()

    override fun extractInformation(result: TranslationResult) {
        val annotatedNode = result.allChildren<Node>({ it.annotations.isNotEmpty() })
        val sfs = annotatedNode.filter { it.annotations.any { it.name.localName == "SF" } }
            .flatMap { it.allChildren<EnumConstantDeclaration>() }
        val securityBs = annotatedNode.filter { it.annotations.any { it.name.lastPartsMatch(Name("TSFI.Behavior")) } }
            .flatMap { it.allChildren<EnumConstantDeclaration>() }
        val sfObjectives = annotatedNode.filter { it.annotations.any { it.name.toString().endsWith("SF.Objectives") } }
        val sfRequirements = annotatedNode.filter {
            it.annotations.any {
                it.name.toString().endsWith(
                    "SF.Requirements"
                )
            }
        }
        val sfActions = annotatedNode.filter {
            it.annotations.any {
                it.name.toString().endsWith(
                    "SF.SecurityActions"
                )
            }
        }
        // val securityBehavior = annotatedNode.filter { it.annotations.any {it.name.endsWith("TSFI.Behavior") } }.flatMap { it.allChildren<EnumConstantDeclaration>() }

        for (sf in sfs) {
            if (!securityFunctionMap.contains(sf.name)) {
                securityFunctionMap.put(
                    sf.name,
                    SecurityFunction(
                        sf.name,
                        (sf.comment ?: "").trimIndent().trim().replace("\n", ""),
                        mutableSetOf(),
                        mutableSetOf(),
                        mutableSetOf()
                    )
                )
            }
        }

        for (securityB in securityBs) {
            securityBehavior.add(securityB.name.toString())
        }

        // Objectives
        sfObjectives.forEach {
            val sfToObjectives: List<Node> = it.allChildren<Node> {
                it.astChildren.any { child ->
                    securityFunctionMap.keys.any { it.localName == child.name.localName }
                }
            }
            sfToObjectives.forEach { parent ->
                parent.astChildren.filter { it is Reference }.firstOrNull()?.let { sf ->
                    val secF = securityFunctionMap.keys.first { it.localName == sf.name.localName }
                    securityFunctionMap[secF]?.objectives?.addAll(
                        parent.allChildren<Literal<String>>().map { it.value ?: "" }
                    )
                }
            }
        }

        // Requirements
        sfRequirements.forEach {
            val sfToRequirements: List<Node> = it.allChildren<Node> {
                it.astChildren.any { child ->
                    securityFunctionMap.keys.any { it.localName == child.name.localName }
                }
            }
            sfToRequirements.forEach { parent ->
                parent.astChildren.filter { it is Reference }.firstOrNull()?.let { sf ->
                    val secF = securityFunctionMap.keys.first { it.localName == sf.name.localName }
                    securityFunctionMap[secF]?.requirements?.addAll(
                        parent.allChildren<Literal<String>>().map { it.value ?: "" }
                    )
                }
            }
        }


        // Actions
        sfActions.forEach {
            val sfToActions: List<Node> = it.allChildren<Node> {
                it.astChildren.any { child ->
                    securityFunctionMap.keys.any { it.localName == child.name.localName }
                }
            }
            sfToActions.forEach { parent ->
                parent.astChildren.filter { it is Reference }.firstOrNull()?.let { sf ->
                    val secF = securityFunctionMap.keys.first { it.localName == sf.name.localName }
                    securityFunctionMap[secF]?.actions?.addAll(
                        parent.allChildren<Literal<String>>().map { Name(it.value ?: "") }
                    )
                }
            }
        }

        securityFunctionMap.values.forEach { sf ->
            sf.actions.forEach { actionName ->
                if (!actionsToSFReverseMap.contains(actionName)) {
                    actionsToSFReverseMap[actionName] = mutableSetOf(sf)
                } else {
                    actionsToSFReverseMap[actionName]?.add(sf)
                }
            }
        }

        val tsfis = annotatedNode.filter { it.annotations.any { it.name.lastPartsMatch(Name("TSFI")) } }
        var preSF = 0
        var postSF = 0
        var identifiedSF = 0

        for (tsfi in tsfis) {
            val tsfiSFs: MutableSet<Name> = mutableSetOf()
            var behavior: Name? = null
            val tsfiAnnotation = tsfi.annotations.filter { it.name.lastPartsMatch(Name("TSFI")) }.first()

            val annotationExtendedNode = mutableSetOf(tsfi)
            if (tsfi is FunctionDeclaration) {
                tsfi.definition?.let { annotationExtendedNode.add(it) }
            }

            if (tsfi is FieldDeclaration) {
                tsfi.definition.let { annotationExtendedNode.add(it) }
            }

            tsfiAnnotation.members.forEach {
                if (it.value is MemberExpression) {
                    behavior = Name(it.value?.code.toString() ?: it.name.toString())
                } else {
                    tsfiSFs.addAll(
                        it.value.allChildren<MemberExpression>().map { Name(it.code.toString()) }.toMutableSet()
                    )
                }

            }

            if (tsfiSFs.isEmpty()) {
                val calls = annotationExtendedNode.flatMap { reachableCalls(it) }
                calls.forEach { call ->
                    actionsToSFReverseMap.keys.forEach { actionKey ->
                        if (call.name.toString().startsWith(actionKey.toString())) {
                            tsfiSFs.addAll(actionsToSFReverseMap[actionKey]?.map { it.name } ?: setOf())
                        }
                    }
                }
                if (tsfiSFs.isEmpty()) {
                    // TODO Here we could investigate why we do not find the SF for it.
                } else {
                    postSF++
                }
            } else {
                preSF++
            }

            val description = annotationExtendedNode.map { it.comment?:"" }.joinToString("").trimIndent().trim().replace("\n","")
            val andContainedFunctions = annotationExtendedNode + annotationExtendedNode.flatMap { it.allChildren<FunctionDeclaration>() }

            val relevantFunctions = andContainedFunctions.filterIsInstance<FunctionDeclaration>().filter { !it.isInferred && !it.isImplicit }.toSet()

            val params = relevantFunctions.flatMap { parseParameters(it).values }.toMutableSet()
            val exceptions = parseExceptions(relevantFunctions).values.toMutableSet()
            val tsfiDeclaration = TSFI(description, behavior?:Name(""), tsfi, annotationExtendedNode,
                tsfiSFs.flatMap { tsfiSFName ->
                    securityFunctionMap.entries.filter { it.key.toString().substringAfterLast(".") ==
                            tsfiSFName.toString().substringAfterLast(".") }.map { it.value } }.toMutableSet(), params, exceptions)
            tsfiDeclarations.add(tsfiDeclaration)
        }
        logger.info { "TSFI: ${tsfiDeclarations.size}: $preSF security functions explicitly specified, $postSF security functions identified through analysis." }

    }

    private fun parseParameters(function:FunctionDeclaration): Map<String, Parameter>{
        val parameterData = mutableMapOf<String, Parameter>()
        for (param in function.parameters){
            parameterData.put(param.name.localName, Parameter(param.name.localName, param.type, ""))
        }
        function.comment?.lines()?.forEach {
            val paramComment = it.substringAfter("@param").trim()
            val name = paramComment.substringBefore(" ")
            val description = paramComment.substringAfter(" ")
            parameterData.get(name)?.description = description.trim()
        }
        return parameterData
    }

    private fun parseExceptions(functions:Set<FunctionDeclaration>): Map<Type,TSFIException> {
        val exceptionsData = mutableMapOf<Type, TSFIException>()

        functions.forEach {function ->
            function.throwsTypes.forEach {
            exceptionsData.put(it, TSFIException(it, "", ""))
        }

            if(exceptionsData.isNotEmpty()){
                function.comment?.lines()?.forEach {
                    if(it.contains("@throws")){
                        val paramComment = it.substringAfter("@throws").trim()
                        val name = paramComment.substringBefore(" ").trim()
                        val description = paramComment.substringAfter(" ").trim()
                        exceptionsData.entries.filter { it.key.name.localName == name }.forEach {
                            it.value.description = description
                        }
                    }
                }
            }
            function.allChildren<UnaryOperator>().filter { it.operatorCode == "throw" }.forEach {
                it.followPrevEOGEdgesUntilHit { it is ConstructExpression }.fulfilled.flatten().filterIsInstance<ConstructExpression>().forEach {
                    val instantiates = it.type
                    val msg = it.arguments.firstOrNull()?.evaluate()
                    val typeBasedMsg = instantiates.name.localName + if(msg != null) (": $msg") else ""

                    var tsfiException:TSFIException? = null
                    if(exceptionsData.contains(instantiates)){
                        tsfiException = exceptionsData[instantiates]
                    }else{
                        tsfiException = getTSFIFromTypeHierarchy(instantiates,exceptionsData)
                    }

                    if(tsfiException != null){
                        if(!tsfiException.message.contains(typeBasedMsg)){
                            tsfiException.message += (if(tsfiException.message.isNotEmpty()) "; " else "") + typeBasedMsg
                        }
                    }else{
                        // Here we could use the message as a description instead of leaving it empty
                        tsfiException = TSFIException(instantiates, typeBasedMsg, "")
                        exceptionsData.put(instantiates,tsfiException)
                    }
                }
            }
        }

        return exceptionsData
    }

    private fun getTSFIFromTypeHierarchy(type: Type, exceptionsData: Map<Type, TSFIException>): TSFIException?{
        if(exceptionsData.isEmpty())
            return null
        var tsfiException: TSFIException? = null
        var hierarchyTypes = setOf(type)
        while (hierarchyTypes.isNotEmpty()){

            hierarchyTypes.forEach {currentType ->
                tsfiException = exceptionsData.entries.filter { it.key.name.localName == currentType.name.localName }.map { it.value }.firstOrNull()
                if(tsfiException != null)
                    return tsfiException
            }
            // BFS in the hierarchy
            hierarchyTypes = hierarchyTypes.flatMap { it.superTypes }.toSet()
        }
        return tsfiException
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


    override fun formatSFInformation(formatter: Formatter): String {
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

    override fun formatTSFIInformation(formatter: Formatter): String {
        var xml = ""
        for (tsfi in tsfiDeclarations){
            var tsfiContent = formatter.format("description", tsfi.description, mapOf())

            var parametersContent = ""
            for(param in tsfi.params){
                parametersContent += formatter.format("parameter", param.description, mapOf("name" to param.name, "type" to param.type.name.toString()))
            }

            if(parametersContent.isEmpty()) parametersContent = " "
            tsfiContent += formatter.format("parameters", parametersContent, mapOf())

            var errorsContent = ""
            for(excepts in tsfi.exceptions){
                var errorContent = ""
                errorContent += formatter.format("message",  if(excepts.message.isNotEmpty()) excepts.message else excepts.type.name.toString(), mapOf())
                errorContent += formatter.format("description", excepts.description, mapOf())

                errorsContent += formatter.format("error", errorContent, mapOf())
            }

            if(errorsContent.isEmpty()) errorsContent = " "
            tsfiContent += formatter.format("errors", errorsContent, mapOf())


            var afContent = ""
            for(sf in tsfi.sf){
                afContent += formatter.format("ref", "", mapOf("target" to replaceSFName(sf.name.toString())))
            }
            if(afContent.isEmpty()) afContent = " "

            tsfiContent += formatter.format("security-functions", afContent, mapOf())


            var actionsContent = ""
            if(actionsContent.isEmpty()) actionsContent = " "

            tsfiContent += formatter.format("actions", actionsContent, mapOf())


            xml += formatter.format("tsfi", tsfiContent, mapOf("id" to tsfi.annotated.name.toString(), "security" to tsfi.securityBehavior.localName.substringAfterLast(".").lowercase()))
        }


        return prettyPrint(formatter.format("functional-specification", xml, mapOf()),2,true)
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
    data class TSFI(val description: String, val securityBehavior:Name, val annotated:Node, val functions:Set<Node>, val sf:MutableSet<SecurityFunction>, val params: MutableSet<Parameter>, val exceptions:MutableSet<TSFIException>)

    data class Parameter(val name: String, val type:Type, var description: String)

    data class TSFIException(val type:Type, var message: String, var description: String)

    data class SecurityFunction(val name:Name, val description:String, val objectives:MutableSet<String>, val requirements: MutableSet<String>, val actions: MutableSet<Name>)
}
