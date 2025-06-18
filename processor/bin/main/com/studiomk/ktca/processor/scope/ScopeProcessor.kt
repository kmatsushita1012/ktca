package com.studiomk.ktca.processor.scope

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.studiomk.ktca.core.annotation.FeatureOf
import java.io.OutputStreamWriter
import com.studiomk.ktca.core.annotation.Prism
import com.studiomk.ktca.core.annotation.Lens

fun KSDeclaration.getScopedTypeName(): String {
    val parent = this.parentDeclaration
    return if (parent is KSClassDeclaration) {
        "${parent.simpleName.asString()}.${this.simpleName.asString()}"
    } else {
        this.simpleName.asString() // fallback
    }
}

class ScopeProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val toReprocess = mutableListOf<KSAnnotated>()

        val featureOfSymbols = resolver.getSymbolsWithAnnotation(FeatureOf::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()
        logger.warn("isNotEmpty ${featureOfSymbols.isNotEmpty()}")
        val lensAnnotations = resolver.getSymbolsWithAnnotation(Lens::class.qualifiedName!!).filterIsInstance<KSPropertyDeclaration>()
        val prismAnnotations = resolver.getSymbolsWithAnnotation(Prism::class.qualifiedName!!).filterIsInstance<KSClassDeclaration>()
        if (featureOfSymbols.isNotEmpty()) {
            // @FeatureOf がまだあれば Lens と Prism のシンボルを再処理リストに追加して待つ
            logger.warn("lensAnnotations ${lensAnnotations.toList()}")
            logger.warn("prismAnnotations ${prismAnnotations.toList()}")
            return (lensAnnotations + prismAnnotations).toList().filterIsInstance<KSAnnotated>()
        }
        logger.warn("lensAnnotations ${lensAnnotations.toList()}")
        logger.warn("prismAnnotations ${prismAnnotations.toList()}")
        val lensProps = lensAnnotations
            .groupBy { it.parentDeclaration?.parentDeclaration as? KSClassDeclaration }
            .filterKeys { it != null }
        val prismActions = prismAnnotations
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
                writer.write("import com.studiomk.ktca.core.scope.Lens\n")
                writer.write("import com.studiomk.ktca.core.scope.Prism\n")
                writer.write("\n")

                // Collect imports for Lens props
                val lensGroup = lensProps[featureDecl] ?: emptyList()
                val prismGroup = prismActions[featureDecl] ?: emptyList()
                logger.warn(featureDecl.toString())
                logger.warn(prismGroup.toString())

                val importSet = mutableSetOf<String>()
                // Lens の importを集める
                for (prop in lensGroup) {
                    logger.warn("import ${ prop.type.resolve().declaration as? KSClassDeclaration}")
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
                    val isNullable: Boolean
//                    logger.error("resolved ${resolved}")
                    if (resolved.isError) {
                        isNullable = false
                        typeName = prop.type.resolve().toString()
                            .removePrefix("<ERROR TYPE: ")
                            .removeSuffix(">")
                    } else {
                        isNullable = resolved.nullability == Nullability.NULLABLE
                        logger.warn("dec ${resolved.declaration as? KSClassDeclaration}")
                        val typeDecl = resolved.declaration as? KSClassDeclaration ?: continue
                        typeName = typeDecl.getScopedTypeName()
                    }
                    writer.write("""
                        val $featureName.${propName}Lens: Lens<${featureName}.State, $typeName${if (isNullable) "?" else ""}>
                            get() = Lens(
                                get = { it.$propName },
                                set = { parent, child -> parent.copy($propName = child) }
                            )
                            
                    """.trimIndent())
                }

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
                        val $featureName.${fieldName}Prism: Prism<${featureName}.Action, $guessedName>
                            get() = Prism(
                                extract = { (it as? ${featureName}.Action.$name)?.action },
                                embed = { ${featureName}.Action.$name(it) }
                            )
                            
                    """.trimIndent())
                }
            }
        }
        logger.warn("toReporocess end ${toReprocess}")
        return toReprocess
    }
}
