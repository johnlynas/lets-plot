/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.vis.svg

import jetbrains.datalore.base.geometry.DoubleRectangle
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.observable.property.Property
import jetbrains.datalore.base.observable.property.WritableProperty
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.vis.svg.SvgShape.Companion.FILL
import jetbrains.datalore.vis.svg.SvgShape.Companion.FILL_OPACITY
import jetbrains.datalore.vis.svg.SvgShape.Companion.STROKE
import jetbrains.datalore.vis.svg.SvgShape.Companion.STROKE_OPACITY
import jetbrains.datalore.vis.svg.SvgShape.Companion.STROKE_WIDTH
import jetbrains.datalore.vis.svg.SvgTransformable.Companion.TRANSFORM

class SvgRectElement() : SvgGraphicsElement(), SvgTransformable,
    SvgShape {

    companion object {
        val X: SvgAttributeSpec<Double> =
            SvgAttributeSpec.createSpec("x")
        val Y: SvgAttributeSpec<Double> =
            SvgAttributeSpec.createSpec("y")
        val WIDTH: SvgAttributeSpec<Double> =
            SvgAttributeSpec.createSpec(SvgConstants.WIDTH)
        val HEIGHT: SvgAttributeSpec<Double> =
            SvgAttributeSpec.createSpec(SvgConstants.HEIGHT)
    }

    override val elementName = "rect"

    override val bBox: DoubleRectangle
        get() = container().getPeer()!!.getBBox(this)

    constructor(x: Double, y: Double, width: Double, height: Double) : this() {

        setAttribute(X, x)
        setAttribute(Y, y)
        setAttribute(HEIGHT, height)
        setAttribute(WIDTH, width)
    }

//    constructor(rect: Rectangle) : this(rect.origin.x, rect.origin.y, rect.dimension.x, rect.dimension.y) {}

    constructor(rect: DoubleRectangle) : this(rect.origin.x, rect.origin.y, rect.dimension.x, rect.dimension.y)

    fun x(): Property<Double?> {
        return getAttribute(X)
    }

    fun y(): Property<Double?> {
        return getAttribute(Y)
    }

    fun height(): Property<Double?> {
        return getAttribute(HEIGHT)
    }

    fun width(): Property<Double?> {
        return getAttribute(WIDTH)
    }

    override fun transform(): Property<SvgTransform?> {
        return getAttribute(TRANSFORM)
    }

    override fun fill(): Property<SvgColor?> {
        return getAttribute(FILL)
    }

    override fun fillColor(): WritableProperty<Color?> {
        return SvgUtils.colorAttributeTransform(fill(), fillOpacity())
    }

    override fun fillOpacity(): Property<Double?> {
        return getAttribute(FILL_OPACITY)
    }

    override fun stroke(): Property<SvgColor?> {
        return getAttribute(STROKE)
    }

    override fun strokeColor(): WritableProperty<Color?> {
        return SvgUtils.colorAttributeTransform(stroke(), strokeOpacity())
    }

    override fun strokeOpacity(): Property<Double?> {
        return getAttribute(STROKE_OPACITY)
    }

    override fun strokeWidth(): Property<Double?> {
        return getAttribute(STROKE_WIDTH)
    }

    override fun pointToTransformedCoordinates(point: DoubleVector): DoubleVector {
        return container().getPeer()!!.invertTransform(this, point)
    }

    override fun pointToAbsoluteCoordinates(point: DoubleVector): DoubleVector {
        return container().getPeer()!!.applyTransform(this, point)
    }
}