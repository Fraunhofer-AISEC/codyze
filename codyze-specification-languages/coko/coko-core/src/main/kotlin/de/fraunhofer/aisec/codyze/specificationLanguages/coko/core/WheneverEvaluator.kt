package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core

class WheneverEvaluator {
    private val ensures: MutableList<Ensure> = mutableListOf()

    fun ensure(block: Ensure.() -> Unit){
        ensures.add(Ensure().apply(block))
    }

    internal fun getEnsures(): List<Ensure> = ensures


}

class Ensure {
    val list = SpecialListBuilder()

}

class SpecialList(elements: Collection<Any>): ArrayList<Any>(elements) {
    override fun contains(element: Any): Boolean {
        //TODO

        return false
    }
}

class SpecialListBuilder {
    operator fun get(vararg things: Any): SpecialList = SpecialList(things.toList())
}