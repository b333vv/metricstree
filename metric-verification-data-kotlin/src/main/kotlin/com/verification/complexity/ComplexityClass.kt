package com.verification.complexity

class ComplexityClass {
    fun simpleMethod() {
        println("Simple method")
    }

    fun complexMethod(input: Int): String {
        return when {
            input < 0 -> "Negative"
            input == 0 -> "Zero"
            input < 10 -> "Small"
            input < 100 -> "Medium"
            else -> "Large"
        }
    }

    fun nestedMethod(a: Int, b: Int): Int {
        var result = 0
        for (i in 0..a) {
            for (j in 0..b) {
                if (i % 2 == 0) {
                    if (j % 2 == 0) {
                        result += i * j
                    }
                }
            }
        }
        return result
    }
}
