package jetbrains.datalore.visualization.plot.gog.core.aes

import jetbrains.datalore.base.typedKey.TypedKey
import jetbrains.datalore.base.typedKey.TypedKeyHashMap
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.visualization.plot.gog.core.render.Aes
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.ALPHA
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.ANGLE
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.COLOR
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.FAMILY
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.FILL
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.FLOW
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.FONTFACE
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.FRAME
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.HEIGHT
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.HJUST
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.INTERCEPT
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.LABEL
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.LINETYPE
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.LOWER
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.MAP_ID
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.MIDDLE
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.SHAPE
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.SIZE
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.SLOPE
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.SPEED
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.UPPER
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.VJUST
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.WEIGHT
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.WIDTH
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.X
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.XEND
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.XINTERCEPT
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.XMAX
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.XMIN
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.Y
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.YEND
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.YINTERCEPT
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.YMAX
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.YMIN
import jetbrains.datalore.visualization.plot.gog.core.render.Aes.Companion.Z
import jetbrains.datalore.visualization.plot.gog.core.render.linetype.NamedLineType
import jetbrains.datalore.visualization.plot.gog.core.render.point.NamedShape

object AesInitValue {

    private val VALUE_MAP = TypedKeyHashMap()

    init {
        VALUE_MAP[X] = 0.0
        VALUE_MAP[Y] = 0.0
        VALUE_MAP[Z] = 0.0
        VALUE_MAP[YMIN] = java.lang.Double.NaN
        VALUE_MAP[YMAX] = java.lang.Double.NaN
        VALUE_MAP[COLOR] = Color.DARK_BLUE
        VALUE_MAP[FILL] = Color.DARK_BLUE
        VALUE_MAP[ALPHA] = 1.0
        VALUE_MAP[SHAPE] = NamedShape.SOLID_CIRCLE
        VALUE_MAP[LINETYPE] = NamedLineType.SOLID
        VALUE_MAP[SIZE] = 0.5  // Line thickness. Should be redefined for other shapes
        VALUE_MAP[WIDTH] = 1.0
        VALUE_MAP[HEIGHT] = 1.0
        VALUE_MAP[WEIGHT] = 1.0
        VALUE_MAP[INTERCEPT] = 0.0
        VALUE_MAP[SLOPE] = 1.0
        VALUE_MAP[XINTERCEPT] = 0.0
        VALUE_MAP[YINTERCEPT] = 0.0
        VALUE_MAP[LOWER] = java.lang.Double.NaN
        VALUE_MAP[MIDDLE] = java.lang.Double.NaN
        VALUE_MAP[UPPER] = java.lang.Double.NaN
        VALUE_MAP[MAP_ID] = "empty map_id"
        VALUE_MAP[FRAME] = "empty frame"
        VALUE_MAP[SPEED] = 10.0
        VALUE_MAP[FLOW] = 0.1
        VALUE_MAP[XMIN] = java.lang.Double.NaN
        VALUE_MAP[XMAX] = java.lang.Double.NaN
        VALUE_MAP[XEND] = java.lang.Double.NaN
        VALUE_MAP[YEND] = java.lang.Double.NaN
        VALUE_MAP[LABEL] = ""
        VALUE_MAP[FAMILY] = "sans-serif"
        VALUE_MAP[FONTFACE] = "plain"
        VALUE_MAP[HJUST] = 0.5  // 'middle'
        VALUE_MAP[VJUST] = 0.5  // 'middle'
        VALUE_MAP[ANGLE] = 0.0
    }

    /**
     * For test only (must br TRUE for any Aes)
     */
    fun has(aes: Aes<*>): Boolean {
        return VALUE_MAP.containsKey(aes)
    }

    operator fun <T> get(aes: Aes<T>): T {
        return VALUE_MAP.get<T>(aes)
    }

    operator fun <T> get(aes: TypedKey<T>): T {
        return VALUE_MAP.get<T>(aes)
    }
}
