
package test.pkg

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

class CboTarget1

class CboCheck {
    val s: java.lang.String = ""
    val t: CboTarget1? = null
}

class ShadowCheck {
    val x = 1
    
    fun method1() {
        val x = 2 // Local x shadows instance x
        print(x) // accesses local x, NOT instance x
    }
    
    fun method2() {
        print(x) // accesses instance x
    }
}

class MpcTarget {
    fun foo() {}
}


class MpcCheck {
    fun m() {
        val t = MpcTarget() // Call to constructor -> 1
        t.foo() // Call to external method -> 1
        this.m() // Call to self -> 0
        print("s") // Call to stdlib -> 0
    }
}


class AtfdForeign {
    var property = 0
    fun getAccessor(): Int = property
    fun setAccessor(v: Int) { property = v }
    fun behavior() {}
}

class BehaviorForeign {
    fun doAction() {}
}

class AtfdCheck {
    private val foreign = AtfdForeign()
    private val behaviorOnly = BehaviorForeign()
    
    fun test() {
        val p = foreign.property // Access property -> count AtfdForeign
        foreign.property = 2     // Set property -> count AtfdForeign
        val a = foreign.getAccessor() // Getter -> count AtfdForeign
        foreign.setAccessor(3)   // Setter -> count AtfdForeign
        foreign.behavior()       // Not data -> don't count
        
        behaviorOnly.doAction() // Not data -> don't count
    }
}
