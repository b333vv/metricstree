class Cohesion {
    private val a: Int = 0
    private val b: Int = 1

    fun f1() {
        val x = a
    }

    fun f2() {
        val y = b
    }

    fun f3() {
        val z = a + b
    }
}
