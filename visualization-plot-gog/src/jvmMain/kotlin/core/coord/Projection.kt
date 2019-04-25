package jetbrains.datalore.visualization.plot.gog.core.coord

import jetbrains.datalore.base.gcommon.collect.ClosedRange

interface Projection {
    fun apply(v: Double?): Double?

    fun toValidDomain(domain: ClosedRange<Double>): ClosedRange<Double>
}
