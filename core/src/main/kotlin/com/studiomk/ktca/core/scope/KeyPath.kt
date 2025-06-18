package com.studiomk.ktca.core.scope

class KeyPath<Parent, Child>(
    val get: (Parent) -> Child,
    val set: (Parent, Child) -> Parent
) {
    operator fun <GrandChild> plus( other: KeyPath<Child, GrandChild>): KeyPath<Parent, GrandChild> = KeyPath(
        get = { parent -> other.get(this.get(parent)) },
        set = { parent, grandChild ->
            val child = this.get(parent)
            if (child != null) {
                this.set(parent, other.set(child, grandChild))
            } else {
                parent
            }
        }
    )
    operator fun <GrandChild> plus( other: OptionalKeyPath<Child, GrandChild>): OptionalKeyPath<Parent, GrandChild> = OptionalKeyPath(
        get = { parent -> other.get(this.get(parent)) },
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