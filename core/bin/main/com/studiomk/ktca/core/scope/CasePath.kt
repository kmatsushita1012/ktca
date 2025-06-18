package com.studiomk.ktca.core.scope

class CasePath<Parent, Child>(
    val extract: (Parent) -> Child?,
    val inject: (Child) -> Parent
) {
    operator fun <GrandChild> plus(other: CasePath<Child, GrandChild>): CasePath<Parent, GrandChild> = CasePath(
        extract = { parent ->
            this.extract(parent)?.let { child ->
                other.extract(child)
            }
        },
        inject = { grandChild ->
            val child = other.inject(grandChild)
            this.inject(child)
        }
    )
}
