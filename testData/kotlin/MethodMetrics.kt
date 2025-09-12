class MethodMetricsHelpers {
    val x: Int = 0
    var y: Int = 0
}

class ForeignA { val z: Int = 1; val q: Int = 2 }
class ForeignB { val z: Int = 3 }

class MethodMetrics {
    val x: Int = 0
    var y: Int = 0

    // CND: depth 2 (if inside if)
    fun cndIf(): Int {
        if (x > 0) {
            if (y > 0) {
                return 1
            }
        }
        return 0
    }

    // CND: when inside if -> depth 2
    fun cndWhen(): Int {
        if (x > 0) {
            when (y) {
                0 -> return 0
                1 -> return 1
                else -> return -1
            }
        }
        return -2
    }

    // LND: depth 3 (for -> while -> do-while)
    fun lndTriple() {
        for (i in 0..10) {
            while (i > 0) {
                do {
                    y = y + 1
                } while (y < 3)
            }
        }
    }

    // NOPM: 3 parameters
    fun nopmFun(a: Int, b: String, c: Double) {}

    // LAA: own accesses 3 (x, this.y, this.x), total accesses 5 (x, this.y, otherA.z, otherB.z, this.x) -> 3/5
    fun laaFun(otherA: ForeignA, otherB: ForeignB) {
        val t = x
        this.y = otherA.z
        val s = otherB.z
        val k = this.x
    }

    // FDP: foreign providers otherA, otherB -> 2 (two distinct receivers)
    fun fdpFun(otherA: ForeignA, otherB: ForeignB) {
        val a = otherA.z
        val b = otherB.z
        val c = otherA.q
    }

    // NOAV: references to a, b, x, y -> 4 unique variable/property names
    fun noavFun(a: Int, b: Int) {
        y = a + b + x + y
    }
}
