package com.verification.coupling

class ClassA {
    private val value: Int = 42

    fun getValue(): Int = value
}

class ClassB {
    private val classA = ClassA()

    fun processValue(): Int {
        return classA.getValue() * 2
    }
}

class ClassC {
    private val classA = ClassA()
    private val classB = ClassB()

    fun complexProcess(): Int {
        return classA.getValue() + classB.processValue()
    }
}
