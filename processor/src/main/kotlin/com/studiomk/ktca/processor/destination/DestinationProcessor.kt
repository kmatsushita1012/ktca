package com.studiomk.ktca.processor.destination

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.OutputStreamWriter
import com.studiomk.ktca.core.annotation.ChildFeature

class DestinationProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(ChildFeature::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        val groupedByParent = symbols.groupBy { it.parentDeclaration as? KSClassDeclaration }

        for ((destinationDecl, cases) in groupedByParent) {
            val destination = destinationDecl ?: continue
            val packageName = destination.packageName.asString()
            val destinationName = destination.simpleName.asString()

            val file = codeGenerator.createNewFile(
                Dependencies(false),
                packageName,
                "${destinationName}Generated"
            )

            OutputStreamWriter(file, Charsets.UTF_8).use { writer ->

                writer.write("package $packageName\n\n")
                writer.write("import com.studiomk.ktca.core.reducer.ReducerOf\n")
                writer.write("import com.studiomk.ktca.core.effect.Effect\n")
                writer.write("import com.studiomk.ktca.core.scope.OptionalKeyPath\n")
                writer.write("import com.studiomk.ktca.core.scope.CasePath\n")
                writer.write("\n")

                //State
                writer.write("sealed class ${destinationName}State {\n")
                for (case in cases) {
                    val name = case.simpleName.asString()

                    val annotation = case.annotations
                        .first { it.shortName.asString() == "ChildFeature" }
                    val reducerClass = annotation.arguments.first().value as KSType
                    val reducerName = reducerClass.declaration.qualifiedName!!.asString()

                    writer.write("    data class $name(val state: $reducerName.State) : ${destinationName}State()\n")
                }
                writer.write("}\n\n")

                //Action
                writer.write("sealed class ${destinationName}Action {\n")
                for (case in cases) {
                    val name = case.simpleName.asString()
                    val annotation = case.annotations
                        .first { it.shortName.asString() == "ChildFeature" }

                    val reducerClass = annotation.arguments.first().value as KSType
                    val reducerName = reducerClass.declaration.qualifiedName!!.asString()

                    writer.write("    data class $name(val action: $reducerName.Action) : ${destinationName}Action()\n")
                }
                writer.write("}\n")

                //Reducer
                writer.write("object ${destinationName}Reducer : ReducerOf<${destinationName}State, ${destinationName}Action> {\n")

                // Reducer fields
                for (case in cases) {
                    val name = case.simpleName.asString()
                    val annotation = case.annotations.first { it.shortName.asString() == "ChildFeature" }
                    val reducerClass = annotation.arguments.first().value as KSType
                    val reducerName = reducerClass.declaration.qualifiedName!!.asString()
                    writer.write("    private val ${name.replaceFirstChar(Char::lowercaseChar)} = $reducerName\n")
                }
                writer.write("\n")

                writer.write("    override fun body() = this\n\n")

                writer.write("    override fun reduce(state: ${destinationName}State, action: ${destinationName}Action): Pair<${destinationName}State, Effect<${destinationName}Action>> {\n")
                writer.write("        return when (state) {\n")

                for (case in cases) {
                    val name = case.simpleName.asString()
                    val param = name.replaceFirstChar(Char::lowercaseChar)
                    val annotation = case.annotations.first { it.shortName.asString() == "ChildFeature" }
                    val reducerClass = annotation.arguments.first().value as KSType
                    val reducerName = reducerClass.declaration.qualifiedName!!.asString()

                    writer.write("            is ${destinationName}State.$name -> when (action) {\n")
                    writer.write("                is ${destinationName}Action.$name -> {\n")
                    writer.write("                    val (newState, effect) = $param.reduce(state.state, action.action)\n")
                    writer.write("                    ${destinationName}State.$name(newState) to effect.map { ${destinationName}Action.$name(it) }\n")
                    writer.write("                }\n")
                    writer.write("                else -> state to Effect.none()\n")
                    writer.write("            }\n")
                }

                writer.write("        }\n")
                writer.write("    }\n")
                writer.write("}\n")

                // Lens/Prism
                for (case in cases) {
                    val name = case.simpleName.asString()
                    val annotation = case.annotations.first { it.shortName.asString() == "ChildFeature" }
                    val reducerClass = annotation.arguments.first().value as KSType
                    val reducerName = reducerClass.declaration.qualifiedName!!.asString()

                    // Destination オブジェクト宣言（すでにあるなら省略可能）
                    val parentDecl = destination.parentDeclaration as? KSClassDeclaration
                    val nestedDestinationName = if (parentDecl != null) {
                        "${parentDecl.simpleName.asString()}.${destination.simpleName.asString()}.$name"
                    } else {
                        "${destination.simpleName.asString()}.$name"
                    }
                    // Lens 拡張プロパティ
                    writer.write("""
                        |
                        |val $nestedDestinationName.key: OptionalKeyPath<${destinationName}State, $reducerName.State> 
                        |    get() = OptionalKeyPath(
                        |        get = { state -> (state as? ${destinationName}State.$name)?.state },
                        |        set = { state, child -> 
                        |           if (child == null) state
                        |           else (state as? DestinationState.Counter1)?.copy(state = child) ?: state
                        |        }
                        |    )
                        |
                        |val ${nestedDestinationName}.case: CasePath<${destinationName}Action, $reducerName.Action>
                        |    get() = CasePath(
                        |        extract = { action -> (action as? ${destinationName}Action.$name)?.action },
                        |        inject = { child -> ${destinationName}Action.$name(child) }
                        |    )
                        |
                    """.trimMargin())
                }

            }
        }

        return emptyList()
    }
}
