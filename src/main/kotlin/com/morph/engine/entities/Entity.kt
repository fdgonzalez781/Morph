package com.morph.engine.entities

data class Entity @JvmOverloads constructor(
        val name: String,
        val id: Int,
        private val components: MutableList<Component> = mutableListOf()
) : Cloneable, Iterable<Component> by components {
    fun addComponent(c: Component) : Entity {
        components.add(c)
        c.init()
        c.parent = this

        return this
    }

    fun <T : Component> removeComponent(c: T) : Entity {
        components.remove(c)
        c.destroy()
        c.parent = null

        return this
    }

    fun clearComponents(type: Class<out Component>) {
        fun destroyComponent(c : Component) {
            c.destroy()
            c.parent = null
        }

        components.filter(type::isInstance).forEach { removeComponent(it) }
    }

    fun clearAllComponents() {
        fun destroyComponent(c : Component) {
            c.destroy()
            c.parent = null
        }

        components.forEach { destroyComponent(it) }
        components.clear()
    }

    inline fun <reified T : Component> getComponent() : T? = this.filterIsInstance<T>().firstOrNull()
    fun <T : Component> getComponent(type: Class<T>) = components.filterIsInstance(type).firstOrNull()

    inline fun <reified T : Component> hasComponent() = getComponent<T>() != null
    fun <T : Component> hasComponent(type: Class<T>) = components.filterIsInstance(type).isNotEmpty()

    fun destroy() {
        clearAllComponents()
    }

    @SafeVarargs
    fun hasComponents(vararg requiredTypes: Class<out Component>) = requiredTypes.all { this.hasComponent(it) }
}

// TODO: Remove
inline fun <reified T: Component> given(e: Entity, block: (T) -> Unit) = e.getComponent<T>()?.let(block)
inline fun <reified T: Component> withComponent(e: Entity, block: T.() -> Unit) = e.getComponent<T>()?.apply(block)

