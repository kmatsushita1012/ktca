package com.studiomk.ktca.core.scope

class OptionalKeyPath<Parent, Child>(
    val get: (Parent) -> Child?,
    val set: (Parent, Child?) -> Parent
) {
    operator fun <GrandChild> plus( other: OptionalKeyPath<Child, GrandChild>): OptionalKeyPath<Parent, GrandChild> = OptionalKeyPath(
        get = { parent -> this.get(parent)?.let { other.get(it) } },
        set = { parent, grandChild ->
            val child = this.get(parent)
            if (child != null) {
                this.set(parent, other.set(child, grandChild))
            } else {
                parent
            }
        }
    )

    operator fun <GrandChild> plus( other: KeyPath<Child, GrandChild>): OptionalKeyPath<Parent, GrandChild> = OptionalKeyPath(
        get = { parent -> this.get(parent)?.let { other.get(it) } },
        set = { parent, grandChild ->
            val child = this.get(parent)
            if (grandChild == null) {
                parent
            }else if (child == null) {
                parent
            } else {
                this.set(parent, other.set(child, grandChild))
            }
        }
    )
}