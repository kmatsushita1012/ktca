package com.studiomk.ktca.core.scope

class Prism<Parent, Child>(
    val extract: (Parent) -> Child?,
    val embed: (Child) -> Parent
) {
    fun <GrandChild> compose(other: Prism<Child, GrandChild>): Prism<Parent, GrandChild> = Prism(
        extract = { parent ->
            this.extract(parent)?.let { child ->
                other.extract(child)
            }
        },
        embed = { grandChild ->
            val child = other.embed(grandChild)
            this.embed(child)
        }
    )
}
