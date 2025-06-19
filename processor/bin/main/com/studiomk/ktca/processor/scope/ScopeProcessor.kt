package com.studiomk.ktca.processor.scope

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.studiomk.ktca.core.annotation.FeatureOf
import java.io.OutputStreamWriter
import com.studiomk.ktca.core.annotation.Cased
import com.studiomk.ktca.core.annotation.Keyed



class ScopeProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val toReprocess = mutableListOf<KSAnnotated>()

        val lensProps = resolver.getSymbolsWithAnnotation(Keyed::class.qualifiedName!!)
            .filterIsInstance<KSPropertyDeclaration>()
            .groupBy { it.parentDeclaration?.parentDeclaration as? KSClassDeclaration }
            .filterKeys { it != null }

        val prismActions = resolver.getSymbolsWithAnnotation(Cased::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .groupBy { it.parentDeclaration?.parentDeclaration as? KSClassDeclaration }
            .filterKeys { it != null }

        val allFeatureDecls = (lensProps.keys + prismActions.keys).filterNotNull()

        for (featureDecl in allFeatureDecls) {
            val packageName = featureDecl.packageName.asString()
            val featureName = featureDecl.simpleName.asString()

            val file = codeGenerator.createNewFile(
                Dependencies(true, featureDecl.containingFile!!), packageName, "ScopeGenerated"
            )

            OutputStreamWriter(file, Charsets.UTF_8).use { writer ->
                writer.write("package $packageName\n\n")
                writer.write("import com.studiomk.ktca.core.scope.KeyPath\n")
                writer.write("import com.studiomk.ktca.core.scope.OptionalKeyPath\n")
                writer.write("import com.studiomk.ktca.core.scope.CasePath\n")
                writer.write("\n")

                // Collect imports for Lens props
                val lensGroup = lensProps[featureDecl] ?: emptyList()
                val prismGroup = prismActions[featureDecl] ?: emptyList()
                val importSet = mutableSetOf<String>()
                // Lens の importを集める
                for (prop in lensGroup) {
                    val typeDecl = prop.type.resolve().declaration as? KSClassDeclaration ?: continue
                    if (typeDecl.qualifiedName == null) {
                        // まだ型解決できていないのでこのシンボルは再処理待ちに回す
                        toReprocess.add(prop)
                        continue
                    }
                    val fullQualifiedName = typeDecl.qualifiedName?.asString()!!
                    val importPath = fullQualifiedName.substringBeforeLast(".")
                    importSet.add("import $importPath")
                }
                importSet.forEach { writer.write("$it\n") }
                writer.write("\n")

                // Lens 拡張関数
                for (prop in lensGroup) {
                    val propName = prop.simpleName.asString()
                    val resolved = prop.type.resolve()
                    val typeName: String
                    if (!resolved.isError&& !resolved.isMarkedNullable) {
                        val typeDecl = resolved.declaration as? KSClassDeclaration ?: continue
                        typeName = typeDecl.getScopedTypeName()
                        writer.write(
                            """
                            val $featureName.${propName}Key: KeyPath<${featureName}.State, $typeName>
                                get() = KeyPath(
                                    get = { it.$propName },
                                    set = { parent, child -> parent.copy($propName = child) }
                                )
                        """.trimIndent()
                        )
                    } else {
                        if(resolved.isError){
                            typeName = prop.type.resolve().toString()
                                .removePrefix("<ERROR TYPE: ")
                                .removeSuffix(">")
                        } else {
                            val typeDecl = resolved.declaration as? KSClassDeclaration ?: continue
                            typeName = typeDecl.getScopedTypeName()
                        }
                        writer.write(
                            """
                            val $featureName.${propName}Key: OptionalKeyPath<${featureName}.State, $typeName>
                                get() = OptionalKeyPath(
                                    get = { it.$propName },
                                    set = { parent, child -> parent.copy($propName = child) }
                                )
                        """.trimIndent())
                    }
                }
                writer.write("\n")
                // Prism 拡張関数
                for (actionClass in prismGroup) {
                    val name = actionClass.simpleName.asString()
                    val fieldName = name.replaceFirstChar(Char::lowercaseChar)
                    val actionProperty = actionClass.getAllProperties()
                        .firstOrNull { it.simpleName.asString() == "action" } ?: continue

                    val guessedName = actionProperty.type.resolve().toString()
                        .removePrefix("<ERROR TYPE: ")
                        .removeSuffix(">")

                    writer.write("""
                        val $featureName.${fieldName}Case: CasePath<${featureName}.Action, $guessedName>
                            get() = CasePath(
                                extract = { (it as? ${featureName}.Action.$name)?.action },
                                inject = { ${featureName}.Action.$name(it) }
                            )
                    """.trimIndent())
                }
            }
        }
        return toReprocess
    }

    fun KSDeclaration.getScopedTypeName(): String {
        val parent = this.parentDeclaration
        return if (parent is KSClassDeclaration) {
            "${parent.simpleName.asString()}.${this.simpleName.asString()}"
        } else {
            this.simpleName.asString() // fallback
        }
    }
}
