package jetbrains.datalore.plot.builder.scale

import jetbrains.datalore.visualization.plot.base.Aes
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultNaValueTest {
    @Test
    fun everyAesHasNaValue() {
        for (aes in Aes.values()) {
            assertTrue(DefaultNaValue.has(aes), "Aes " + aes.name + " has n/a value")
        }
    }
}