package com.verification.inheritance

abstract class BaseClass {
    abstract fun abstractMethod()

    open fun overridableMethod() {
        println("Base implementation")
    }

    fun finalMethod() {
        println("Cannot be overridden")
    }
}

open class MiddleClass : BaseClass() {
    override fun abstractMethod() {
        println("Middle class implementation")
    }

    override fun overridableMethod() {
        println("Middle class override")
    }

    open fun additionalMethod() {
        println("Additional functionality")
    }
}

class LeafClass : MiddleClass() {
    override fun abstractMethod() {
        println("Leaf class implementation")
    }

    override fun overridableMethod() {
        super.overridableMethod()
        println("Leaf class additional behavior")
    }

    override fun additionalMethod() {
        println("Leaf class version")
    }
}
