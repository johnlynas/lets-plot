package jetbrains.datalore.base.composite

import jetbrains.datalore.base.composite.TestComposite.Companion.create
import kotlin.test.Test
import kotlin.test.assertEquals

class UpperLowerFocusableTest {

    @Test
    fun upperFocusable() {
        val root = create(0, 0, 10, 2)

        val row1 = create(0, 0, 10, 1)
        val row2 = create(0, 1, 10, 1)

        val left = create(1, 0, 3, 1)
        val right = create(5, 0, 3, 1)

        row1.children().addAll(listOf(left, right))
        root.children().addAll(listOf(row1, row2))

        assertEquals(listOf(right, left), CompositesWithBounds(0).upperFocusables(row2).toList())
    }

    @Test
    fun lowerFocusable() {
        val root = create(0, 0, 10, 2)

        val row1 = create(0, 0, 10, 1)
        val row2 = create(0, 1, 10, 1)

        val left = create(1, 1, 5, 1)
        val right = create(8, 1, 1, 1)

        row2.children().addAll(listOf(left, right))

        root.children().addAll(listOf(row1, row2))

        assertEquals(listOf(left, right), CompositesWithBounds(0).lowerFocusables(row1).toList())
    }
}