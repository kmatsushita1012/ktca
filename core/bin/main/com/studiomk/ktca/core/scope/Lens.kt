package com.studiomk.ktca.core.scope

class Lens<Parent, Child>(
    val get: (Parent) -> Child,
    val set: (Parent, Child) -> Parent
) {
    fun <GrandChild> compose(other: Lens<Child, GrandChild>): Lens<Parent, GrandChild> = Lens(
        get = { s -> other.get(this.get(s)) },
        set = { s, b -> this.set(s, other.set(this.get(s), b)) }
    )
}