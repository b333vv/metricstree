class CognitiveCases {
    // if-else-if chain and boolean chain
    fun ifElseBoolean(a: Int, b: Int, c: Int): Int {
        if (a > 0 && b > 0 || c > 0) {
            return 1
        } else if (a < 0) {
            return -1
        } else {
            return 0
        }
    }

    // recursion
    fun factorial(n: Int): Int {
        if (n <= 1) return 1
        return n * factorial(n - 1)
    }

    // labeled loops and nesting
    fun labeledLoops(xs: List<Int>): Int {
        var count = 0
        outer@ for (x in xs) {
            var i = x
            while (i > 0) {
                if (i % 2 == 0) {
                    count++
                    break@outer
                }
                i--
            }
        }
        return count
    }

    // when expression
    fun whenExpr(v: Int): Int = when (v) {
        0 -> 0
        in 1..10 -> 1
        else -> -1
    }
}
