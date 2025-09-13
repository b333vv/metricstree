package com.verification.cohesion

class CohesionClass {
    private val field1: String = ""
    private val field2: Int = 0

    fun method1() {
        println(field1)
    }

    fun method2() {
        println(field2)
    }

    fun method3() {
        println(field1 + field2)
    }
}
