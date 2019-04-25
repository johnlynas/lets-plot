package jetbrains.datalore.visualization.plot.gog.core.data

import java.util.function.Function

interface GroupAwareSampling : Sampling {
    fun isApplicable(population: DataFrame, groupMapper: Function<Int, Int>): Boolean

    fun apply(population: DataFrame, groupMapper: Function<Int, Int>): DataFrame
}
