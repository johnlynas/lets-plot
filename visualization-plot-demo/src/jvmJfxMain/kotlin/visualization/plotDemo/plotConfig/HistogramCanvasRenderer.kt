package jetbrains.datalore.visualization.plotDemo.plotConfig

import jetbrains.datalore.visualization.demoUtils.jfx.CanvasRendererDemoFactory
import jetbrains.datalore.visualization.plotDemo.model.plotConfig.Histogram

object HistogramCanvasRenderer {
    @JvmStatic
    fun main(args: Array<String>) {
        with(Histogram()) {
            @Suppress("UNCHECKED_CAST")
            val plotSpecList = plotSpecList() as List<MutableMap<String, Any>>
            PlotConfigDemoUtil.show("Histogram", plotSpecList, CanvasRendererDemoFactory(), demoComponentSize)
        }
    }
}
