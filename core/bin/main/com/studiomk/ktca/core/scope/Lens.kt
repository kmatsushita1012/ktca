package com.studiomk.ktca.core.scope

class Lens<Parent, Child>(
    val get: (Parent) -> Child,
    val set: (Parent, Child) -> Parent
) {
    operator fun <GrandChild> plus( other: Lens<Child, GrandChild>): Lens<Parent, GrandChild> = Lens(
        get = { parent -> other.get(this.get(parent))},
        set = { parent, grandChild ->
            val child = this.get(parent)
            if (child != null) {
                this.set(parent, other.set(child, grandChild))
            } else {
                parent
            }
        }
    )
}