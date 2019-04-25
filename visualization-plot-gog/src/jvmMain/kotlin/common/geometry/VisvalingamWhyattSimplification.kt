package jetbrains.datalore.visualization.plot.gog.common.geometry

import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.visualization.plot.gog.common.geometry.PolylineSimplifier.RankingStrategy
import java.util.*
import java.util.Collections.nCopies


// Reference: https://bost.ocks.org/mike/simplify/
internal class VisvalingamWhyattSimplification : RankingStrategy {
    private val myVerticesToRemove = ArrayList<Int>()
    private var myTriangles: MutableList<Triangle>? = null

    private val isSimplificationDone: Boolean
        get() = isEmpty

    private val isEmpty: Boolean
        get() = myTriangles!!.isEmpty()

    override fun getWeights(points: List<DoubleVector>): List<Double> {
        myTriangles = ArrayList(points.size - 2)
        initTriangles(points)
        val weights = ArrayList(nCopies(points.size, INITIAL_AREA))
        var lastRemovedVertexArea = 0.0
        while (!isSimplificationDone) {
            val triangle = takeTriangle()

            lastRemovedVertexArea = if (triangle.area > lastRemovedVertexArea)
                triangle.area
            else
                lastRemovedVertexArea

            weights[triangle.currentVertex] = lastRemovedVertexArea

            val next = triangle.next
            if (next != null) {
                next.takePrevFrom(triangle)
                update(next)
            }

            val prev = triangle.prev
            if (prev != null) {
                prev.takeNextFrom(triangle)
                update(prev)
            }

            myVerticesToRemove.add(triangle.currentVertex)
        }

        return weights
    }

    private fun initTriangles(points: List<DoubleVector>) {
        val triangles = ArrayList<Triangle>(points.size - 2)

        run {
            var i = 1
            val n = points.size - 1
            while (i < n) {
                triangles.add(Triangle(i, points))
                ++i
            }
        }

        var i = 1
        val n = triangles.size - 1
        while (i < n) {
            triangles[i].next = triangles[i + 1]
            triangles[i].prev = triangles[i - 1]
            i++
        }

        triangles.forEach { this.add(it) }
    }

    private fun takeTriangle(): Triangle {
        val minimalTriangle = poll()
        myVerticesToRemove.add(minimalTriangle.currentVertex)
        return minimalTriangle
    }

    private fun add(triangle: Triangle) {
        val index = getIndex(triangle)
        myTriangles!!.add(index, triangle)
    }

    private fun getIndex(triangle: Triangle): Int {
        var index = Collections.binarySearch(myTriangles!!, triangle, Comparator.comparingDouble { t -> t.area })
        if (index < 0) {
            index = index.inv()
        }
        return index
    }

    private fun peek(): Triangle {
        return myTriangles!![0]
    }

    private fun poll(): Triangle {
        val triangle = peek()
        myTriangles!!.remove(triangle)
        return triangle
    }

    private fun update(triangle: Triangle) {
        myTriangles!!.remove(triangle)
        myTriangles!!.add(triangle)
    }

    private class Triangle internal constructor(val currentVertex: Int, private val myPoints: List<DoubleVector>) {
        var area: Double = 0.toDouble()
            private set
        private var prevVertex: Int = 0
        private var nextVertex: Int = 0
        var prev: Triangle? = null
        var next: Triangle? = null

        init {
            prevVertex = currentVertex - 1
            nextVertex = currentVertex + 1
            area = calculateArea()
        }

        internal fun takeNextFrom(triangle: Triangle) {
            next = triangle.next
            nextVertex = triangle.nextVertex
            area = calculateArea()
        }

        internal fun takePrevFrom(triangle: Triangle) {
            prev = triangle.prev
            prevVertex = triangle.prevVertex
            area = calculateArea()
        }

        private fun calculateArea(): Double {
            val a = myPoints[prevVertex]
            val b = myPoints[currentVertex]
            val c = myPoints[nextVertex]

            return Math.abs(((b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y)) / 2.0)
        }
    }

    companion object {

        private val INITIAL_AREA = java.lang.Double.MAX_VALUE
    }
}