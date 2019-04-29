package jetbrains.datalore.visualization.plot.gog.config

import jetbrains.datalore.base.function.MyRunnable
import jetbrains.datalore.visualization.plot.gog.config.Option.Guide.COLOR_BAR
import jetbrains.datalore.visualization.plot.gog.config.Option.Guide.COLOR_BAR_GB
import jetbrains.datalore.visualization.plot.gog.config.Option.Guide.ColorBar.BIN_COUNT
import jetbrains.datalore.visualization.plot.gog.config.Option.Guide.ColorBar.HEIGHT
import jetbrains.datalore.visualization.plot.gog.config.Option.Guide.ColorBar.WIDTH
import jetbrains.datalore.visualization.plot.gog.config.Option.Guide.Legend.BY_ROW
import jetbrains.datalore.visualization.plot.gog.config.Option.Guide.Legend.COL_COUNT
import jetbrains.datalore.visualization.plot.gog.config.Option.Guide.Legend.ROW_COUNT
import jetbrains.datalore.visualization.plot.gog.config.Option.Guide.NONE
import jetbrains.datalore.visualization.plot.gog.config.Option.Guide.REVERSE
import jetbrains.datalore.visualization.plot.gog.plot.ColorBarOptions
import jetbrains.datalore.visualization.plot.gog.plot.GuideOptions
import jetbrains.datalore.visualization.plot.gog.plot.LegendOptions

internal abstract class GuideConfig private constructor(opts: Map<String, Any>) : OptionsAccessor(opts, emptyMap<Any, Any>()) {

    fun createGuideOptions(): GuideOptions {
        val options = createGuideOptionsIntern()
        options.isReverse = getBoolean(REVERSE)
        return options
    }

    protected abstract fun createGuideOptionsIntern(): GuideOptions

    protected fun trySafe(r: MyRunnable) {
        try {
            r()
        } catch (ignored: RuntimeException) {
            // ok
        }

    }


    private class GuideNoneConfig internal constructor() : GuideConfig(emptyMap()) {

        override fun createGuideOptionsIntern(): GuideOptions {
            return GuideOptions.NONE
        }
    }

    private class LegendConfig internal constructor(opts: Map<String, Any>) : GuideConfig(opts) {

        override fun createGuideOptionsIntern(): GuideOptions {
            val options = LegendOptions()
            trySafe({ options.colCount = getDouble(COL_COUNT)!!.toInt() })
            trySafe({ options.rowCount = getDouble(ROW_COUNT)!!.toInt() })

            options.isByRow = getBoolean(BY_ROW)
            return options
        }
    }

    private class ColorBarConfig internal constructor(opts: Map<String, Any>) : GuideConfig(opts) {

        override fun createGuideOptionsIntern(): GuideOptions {
            val options = ColorBarOptions()
            trySafe({ options.width = getDouble(WIDTH) })
            trySafe({ options.height = getDouble(HEIGHT) })
            trySafe({ options.binCount = getDouble(BIN_COUNT)!!.toInt() })
            return options
        }
    }

    companion object {
        fun create(guide: Any): GuideConfig {
            // guide - name (legend/colorbar)
            //        or
            //        map with guide options
            if (guide is Map<*, *>) {
                val guideOptions = guide as Map<String, Any>
                return createForName(ConfigUtil.featureName(guideOptions), guideOptions)
            }
            return createForName(guide.toString(), HashMap())
        }

        private fun createForName(name: String, guideOptions: Map<String, Any>): GuideConfig {
            if (COLOR_BAR == name || COLOR_BAR_GB == name) {
                return ColorBarConfig(guideOptions)
            }
            return if (NONE == name) {
                GuideNoneConfig()
            } else LegendConfig(guideOptions)
        }
    }
}
