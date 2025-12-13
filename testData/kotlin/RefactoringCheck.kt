
package kotlin

data class DataClass(val x: Int, var y: String)

class PropertyClass {
    val prop1: Int = 1
    var prop2: String = "s"
    
    fun explicitMethod() {
        val a = prop1
        prop2 = "changed"
    }
}

class ComplexityClass {
    fun elvis(x: String?): Int {
        val len = x?.length ?: 0 // Safe call (+0 CCM) and Elvis (+1 CC, +1 CCM)
        return len
    }
    
    fun customAccessors() {
        // just a holder
    }
}

class CustomPropertyClass {
    var complexProp: Int = 0
        get() {
            if (field > 0) return field else return 0
        }
        set(value) {
            if (value >= 0) field = value
        }
}

internal class InternalClass {
    internal val internalProp = 1
    val publicProp = 2
}

open class OpenClass
class FinalClass : OpenClass()
