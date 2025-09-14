package com.verification.lanza

// For NOAC (Number Of Accessor Methods) and NOPA (Number Of Public Attributes)
class AccessorsAndForeignData {
    // public properties (Kotlin default is public)
    val pubVal: Int = 1
    var pubVar: Int = 2
}

class ForeignProviderA { val dataA: Int = 10 }
class ForeignProviderB { val dataB: Int = 20 }

// For ATFD (Access To Foreign Data) and LAA (Locality Of Attribute Accesses)
class ForeignDataConsumer {
    val x: Int = 0
    var y: Int = 0

    fun compute(a: ForeignProviderA, b: ForeignProviderB): Int {
        // own: x, y ; foreign: a.dataA, b.dataB
        val t = x
        y = a.dataA + b.dataB
        return t + y
    }
}
