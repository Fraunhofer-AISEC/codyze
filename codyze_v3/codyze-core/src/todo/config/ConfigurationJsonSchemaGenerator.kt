package de.fraunhofer.aisec.codyze_core.config

import com.fasterxml.classmate.ResolvedType
import com.fasterxml.jackson.databind.JsonNode
import com.github.victools.jsonschema.generator.*
import com.github.victools.jsonschema.module.jackson.JacksonModule
import java.io.File

class ConfigurationJsonSchemaGenerator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val module = JacksonModule()
            val configBuilder =
                SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON)
                    .with(module)
            configBuilder
                .with(Option.INLINE_ALL_SCHEMAS)
                .forTypesInGeneral()
                .withTitleResolver { scope: TypeScope ->
                    if (scope.type.erasedType == Configuration::class.java)
                        "Codyze Configuration File Schema"
                    else null
                }
                .withDescriptionResolver { scope: TypeScope ->
                    if (scope.type.erasedType == Configuration::class.java)
                        "A schema for writing YAML configuration files for codyze"
                    else null
                }
                .withCustomDefinitionProvider {
                    javaType: ResolvedType,
                    context: SchemaGenerationContext ->
                    if (javaType.isInstanceOf(File::class.java))
                        CustomDefinition(
                            context.createDefinitionReference(
                                context.typeContext.resolve(String::class.java)
                            )
                        )
                    else null
                }
                .withPropertySorter { m1: MemberScope<*, *>?, m2: MemberScope<*, *>? -> 1 }
            val co = configBuilder.build()
            val generator = SchemaGenerator(co)
            val jsonSchema: JsonNode = generator.generateSchema(Configuration::class.java)
            jsonSchema.toPrettyString()
            if (args.isNotEmpty()) {
                val output = File(args[0], "codyze-config-schema.json")
                output.writeText(jsonSchema.toPrettyString())
            }
        }
    }
}
