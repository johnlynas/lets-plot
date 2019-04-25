package jetbrains.datalore.visualization.plot.gog.core.render.linetype

import java.util.Arrays.asList

enum class NamedLineType private constructor(val code: Int, private val myDashArray: List<Double>?) : LineType {
    // 0 = blank, 1 = solid, 2 = dashed, 3 = dotted, 4 = dotdash, 5 = longdash, 6 = twodash
    BLANK(0, null) {
        override val isBlank: Boolean
            get() = true
    },
    SOLID(1, null) {
        override val isSolid: Boolean
            get() = true
    },
    DASHED(2, asList<Double>(4.3, 4.3)),
    DOTTED(3, asList<Double>(1.0, 3.2)),
    DOTDASH(4, asList<Double>(1.0, 3.2, 4.3, 3.2)),
    LONGDASH(5, asList<Double>(7.4, 3.2)),
    TWODASH(6, asList<Double>(2.4, 2.4, 6.4, 2.4));

    override val isSolid: Boolean
        get() = false

    override val isBlank: Boolean
        get() = false

    override val dashArray: List<Double>
        get() {
            if (!(isSolid || isBlank)) {
                return myDashArray!!
            }
            throw IllegalStateException("No dash array in " + name.toLowerCase() + " linetype")
        }
}