package jetbrains.datalore.visualization.plot.gog.core.render.geom

import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.visualization.base.svg.slim.SvgSlimElements
import jetbrains.datalore.visualization.base.svg.slim.SvgSlimGroup
import jetbrains.datalore.visualization.base.svg.slim.SvgSlimObject
import jetbrains.datalore.visualization.plot.gog.common.data.SeriesUtil
import jetbrains.datalore.visualization.plot.gog.core.event3.GeomTargetCollector
import jetbrains.datalore.visualization.plot.gog.core.event3.GeomTargetCollector.TooltipParams
import jetbrains.datalore.visualization.plot.gog.core.event3.GeomTargetCollector.TooltipParams.Companion.params
import jetbrains.datalore.visualization.plot.gog.core.render.Aes
import jetbrains.datalore.visualization.plot.gog.core.render.Aesthetics
import jetbrains.datalore.visualization.plot.gog.core.render.AestheticsUtil
import jetbrains.datalore.visualization.plot.gog.core.render.CoordinateSystem
import jetbrains.datalore.visualization.plot.gog.core.render.DataPointAesthetics
import jetbrains.datalore.visualization.plot.gog.core.render.GeomContext
import jetbrains.datalore.visualization.plot.gog.core.render.LegendKeyElementFactory
import jetbrains.datalore.visualization.plot.gog.core.render.PositionAdjustment
import jetbrains.datalore.visualization.plot.gog.core.render.SvgRoot
import jetbrains.datalore.visualization.plot.gog.core.render.geom.util.GeomHelper
import jetbrains.datalore.visualization.plot.gog.core.render.point.NamedShape
import jetbrains.datalore.visualization.plot.gog.core.render.point.PointShape
import jetbrains.datalore.visualization.plot.gog.core.render.point.PointShapes

import java.util.Arrays

import jetbrains.datalore.visualization.plot.gog.core.render.geom.util.HintColorUtil.fromColorValue

internal class PointGeom : GeomBase() {

    var animation: Any? = null

    override val legendKeyElementFactory: LegendKeyElementFactory
        get() = PointLegendKeyElementFactory()

    public override fun buildIntern(root: SvgRoot, aesthetics: Aesthetics, pos: PositionAdjustment, coord: CoordinateSystem, ctx: GeomContext) {
        val helper = GeomHelper(pos, coord, ctx)
        val targetCollector = getGeomTargetCollector(ctx)

        val count = aesthetics.dataPointCount()
        val slimGroup = SvgSlimElements.g(count)
        for (i in 0 until count) {
            val p = aesthetics.dataPointAt(i)
            val x = p.x()
            val y = p.y()

            if (SeriesUtil.allFinite(x, y)) {
                val location = helper.toClient(DoubleVector(x!!, y!!), p)

                val shape = p.shape()!!
                targetCollector.addPoint(i, location, shape.size(p) / 2, getTooltipParams(p))
                val o = shape.create(location, p)
                o.appendTo(slimGroup)
            }
        }
        root.add(wrap(slimGroup))
    }

    private fun getTooltipParams(p: DataPointAesthetics): TooltipParams {
        var color = Color.TRANSPARENT
        if (p.shape()!!.code == PointShapes.dot().code) {
            color = p.color()!!
        } else if (p.shape() is NamedShape) {
            val shape = p.shape() as NamedShape
            color = AestheticsUtil.fill(shape.isFilled, shape.isSolid, p)
        }

        return params().setColor(fromColorValue(color, p.alpha()!!))
    }

    companion object {
        val RENDERS = Arrays.asList<Aes<*>>(
                Aes.X,
                Aes.Y,
                Aes.SIZE,
                Aes.COLOR,
                Aes.FILL,
                Aes.ALPHA,
                Aes.SHAPE,
                Aes.MAP_ID
                // strokeWidth
        )

        val HANDLES_GROUPS = false
    }
}

