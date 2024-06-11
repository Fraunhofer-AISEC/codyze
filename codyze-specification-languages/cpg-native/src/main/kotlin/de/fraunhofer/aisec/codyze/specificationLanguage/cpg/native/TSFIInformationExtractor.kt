package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.Annotation
import de.fraunhofer.aisec.cpg.graph.declarations.EnumConstantDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.FieldDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Reference
import de.fraunhofer.aisec.cpg.passes.astParent

class TSFIInformationExtractor: InformationExtractor() {

    val securityFunctionMap: MutableMap<Name, SecurityFunction> = mutableMapOf()

    val securityBehavior: MutableSet<String> = mutableSetOf()

    /**
     * Reverse map to more efficiently find SFs related to an appearance of an action.
     */
    val actionsToSFReverseMap: Map<Name,SecurityFunction> = mutableMapOf()

    override fun extractInformation(result: TranslationResult) {
        val annotatedNode = result.allChildren<Node>({ it.annotations.isNotEmpty()})
        val sfs = annotatedNode.filter { it.annotations.any {it.name.localName == "SF" } }
            .flatMap { it.allChildren<EnumConstantDeclaration>() }
        val securityBs = annotatedNode.filter { it.annotations.any {it.name.lastPartsMatch(Name("TSFI.Behavior")) } }
            .flatMap { it.allChildren<EnumConstantDeclaration>() }
        val sfOptions = annotatedNode.filter { it.annotations.any { it.name.toString().endsWith("SF.Options") } }
        val sfRequirements = annotatedNode.filter { it.annotations.any { it.name.toString().endsWith("SF.Requirements") } }
        val sfActions = annotatedNode.filter { it.annotations.any { it.name.toString().endsWith("SF.SecurityActions") } }
        //val securityBehavior = annotatedNode.filter { it.annotations.any {it.name.endsWith("TSFI.Behavior") } }.flatMap { it.allChildren<EnumConstantDeclaration>() }

        for (sf in sfs){
            if (!securityFunctionMap.contains(sf.name)){
                securityFunctionMap.put(sf.name, SecurityFunction(sf.name.toString(),sf.comment?:"",
                    mutableSetOf(), mutableSetOf(), mutableSetOf(), mutableSetOf()))
            }
        }

        for (securityB in securityBs){
            securityBehavior.add(securityB.name.toString())
        }

        // Options
        sfOptions.forEach {
            val sfDefinitions: List<Node> = it.allChildren<Node> { it.astChildren.any { child ->
                securityFunctionMap.keys.any { it.lastPartsMatch(child.name) } } }
            sfDefinitions.forEach {parent ->
                it.astChildren.filter { it is Reference }.firstOrNull()?.let {sf ->
                    val secF = securityFunctionMap.keys.first { it.lastPartsMatch(sf.name) }
                    securityFunctionMap.get(secF)?.options?.addAll(parent.allChildren<Literal<String>>().map { it.name.toString() })
                }
            }
        }

        // Requirements
        sfRequirements.forEach {
            val sfDefinitions: List<Node> = it.allChildren<Node> { it.astChildren.any { child ->
                securityFunctionMap.keys.any { it.lastPartsMatch(child.name) } } }
            sfDefinitions.forEach {parent ->
                it.astChildren.filter { it is Reference }.firstOrNull()?.let {sf ->
                    val secF = securityFunctionMap.keys.first { it.lastPartsMatch(sf.name) }
                    securityFunctionMap.get(secF)?.requirements?.addAll(parent.allChildren<Literal<String>>().map { it.name.toString() })
                }
            }
        }


        // Actions
        sfActions.forEach {
            val sfDefinitions: List<Node> = it.allChildren<Node> { it.astChildren.any { child ->
                securityFunctionMap.keys.any { it.lastPartsMatch(child.name) } } }
            sfDefinitions.forEach {parent ->
                it.astChildren.filter { it is Reference }.firstOrNull()?.let {sf ->
                    val secF = securityFunctionMap.keys.first { it.lastPartsMatch(sf.name) }
                    securityFunctionMap.get(secF)?.actions?.addAll(parent.allChildren<Literal<String>>().map { it.name.toString() })
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
                    // TODO we need an inverse map from actions to SFs
                    // Todo parse out tsfis, search for all eog and invokes reachable calls and associate them to a sf based on the
                    // parsedfunction name

                    // Todo find all nested EOG starters and perform a reachability analsis following EOG and invokes, getting all CallExpressions

                }

            }

            val tsfiDeclaration = TSFI(behavior?:Name(""), annotationExtendedNode)
            tsfiSFs.forEach {
                securityFunctionMap[it]?.tsfis?.add(tsfiDeclaration)
            }
        }

    }


    override fun formatInformation(formatter: Formatter): String {
        return ""
    }

    /**
     * In contrast to the annotation the data class does not contain the security function.When the TSFIs are parsed, the
     * associated security functions are either provided in the annotation or identified through static code analysis.
     */
    data class TSFI(val securityBehavior:Name, val functions:Set<Node>)

    data class SecurityFunction(val name:String, val description:String,val options:MutableSet<String>, val requirements: MutableSet<String>, val actions: MutableSet<String>, val tsfis:MutableSet<TSFI>)
}