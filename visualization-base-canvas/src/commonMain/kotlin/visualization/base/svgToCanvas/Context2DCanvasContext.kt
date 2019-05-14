package jetbrains.datalore.visualization.base.svgToCanvas

import jetbrains.datalore.base.values.Color
import jetbrains.datalore.base.values.Colors
import jetbrains.datalore.visualization.base.canvas.Context2d
import jetbrains.datalore.visualization.base.canvas.Context2d.LineJoin.BEVEL
import jetbrains.datalore.visualization.base.canvas.Context2d.TextBaseline.ALPHABETIC
import jetbrains.datalore.visualization.base.canvas.CssStyleUtil.extractStyleFont
import jetbrains.datalore.visualization.base.canvas.svgToCanvas.toRadians
import jetbrains.datalore.visualization.base.svg.SvgTransform
import jetbrains.datalore.visualization.base.svg.slim.CanvasContext
import jetbrains.datalore.visualization.base.svgToCanvas.ParsingUtil.Result
import kotlin.math.PI
import kotlin.math.tan

internal class Context2DCanvasContext(private val myContext: Context2d) : CanvasContext {

    override fun push(transform: Any?) {
        myContext.save()
        if (transform != null) {
            applyTransform(ParsingUtil.parseTransform(transform.toString()))
        }
    }

    override fun restore() {
        myContext.restore()
    }

    private fun stroke(isStrokeRequired: Boolean, strokeOpacity: Double) {
        if (isStrokeRequired) {
            myContext.setGlobalAlpha(strokeOpacity)
            myContext.stroke()
        }
    }

    private fun parseColorString(colorString: String?): Color? {
        return if (colorString == null || "none" == colorString)
            null
        else if (colorString.startsWith('#')) {
            Color.parseHex(colorString)
        } else if (Colors.isColorName(colorString)) {
            Colors.forName(colorString)
        } else {
            Color.parseColor(colorString)
        }
    }

    private fun drawNextElement(lineDash: DoubleArray?, transform: String?, fillColor: Color?,
                                strokeColor: Color?, strokeWidth: Double) {
        myContext.save()
        myContext.setFillColor(fillColor)
        myContext.setStrokeColor(strokeColor)
        myContext.setLineWidth(strokeWidth)
        if (lineDash != null) {
            myContext.setLineDash(lineDash)
        }
        if (transform != null) {
            applyTransform(ParsingUtil.parseTransform(transform))
        }
    }

    override fun drawCircle(cx: Double, cy: Double, r: Double, lineDash: DoubleArray?, transform: String?,
                            fillColor: String?, fillOpacity: Double, strokeColor: String?, strokeOpacity: Double, strokeWidth: Double) {
        val fillColor = parseColorString(fillColor)
        val strokeColor = parseColorString(strokeColor)
        drawNextElement(lineDash, transform, fillColor, strokeColor, strokeWidth)
        myContext.beginPath()
        myContext.arc(cx, cy, r, 0.0, 2 * PI)
        myContext.setGlobalAlpha(fillOpacity)
        myContext.fill()
        stroke(strokeColor != null, strokeOpacity)
        restore()
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double, lineDash: DoubleArray?, transform: String?,
                          strokeColor: String?, strokeOpacity: Double, strokeWidth: Double) {
        val strokeColor = parseColorString(strokeColor)
        drawNextElement(lineDash, transform, null, strokeColor, strokeWidth)
        myContext.setGlobalAlpha(strokeOpacity)
        myContext.beginPath()
        myContext.moveTo(x1, y1)
        myContext.lineTo(x2, y2)
        myContext.stroke()
        restore()
    }

    override fun drawRect(x: Double, y: Double, width: Double, height: Double, lineDash: DoubleArray?, transform: String?,
                          fillColor: String?, fillOpacity: Double, strokeColor: String?, strokeOpacity: Double, strokeWidth: Double) {
        val fillColor = parseColorString(fillColor)
        val strokeColor = parseColorString(strokeColor)
        drawNextElement(lineDash, transform, fillColor, strokeColor, strokeWidth)
        if (fillColor != null) {
            myContext.setGlobalAlpha(fillOpacity)
            myContext.fillRect(x, y, width, height)
        }
        if (strokeColor != null) {
            myContext.setGlobalAlpha(strokeOpacity)
            myContext.strokeRect(x, y, width, height)
        }
        restore()
    }

    override fun drawPath(d: String?, lineDash: DoubleArray?, transform: String?,
                          fillColor: String?, fillOpacity: Double, strokeColor: String?, strokeOpacity: Double, strokeWidth: Double) {
        val fillColor = parseColorString(fillColor)
        val strokeColor = parseColorString(strokeColor)
        drawNextElement(lineDash, transform, fillColor, strokeColor, strokeWidth)
        myContext.setLineJoin(BEVEL)
        myContext.beginPath()
        PathProcessor.apply(d, myContext)
        myContext.setGlobalAlpha(fillOpacity)
        myContext.fillEvenOdd()
        stroke(strokeColor != null, strokeOpacity)
        restore()
    }

    override fun drawText(x: Double, y: Double, text: String, style: String?, transform: String?,
                          fillColor: String?, fillOpacity: Double, strokeColor: String?, strokeOpacity: Double, strokeWidth: Double,
                          textAnchor: String?, textDy: String?) {
        val fillColor = parseColorString(fillColor)
        val strokeColor = parseColorString(strokeColor)
        drawNextElement(null, transform, fillColor, strokeColor, strokeWidth)
        myContext.setTextBaseline(ALPHABETIC)
        myContext.setFont(extractStyleFont(style) ?: DEFAULT_FONT)
        if (strokeColor != null && strokeWidth > 0) {
            myContext.setGlobalAlpha(strokeOpacity)
            myContext.strokeText(text, x, y)
        }
        myContext.setGlobalAlpha(fillOpacity)
        myContext.setTextAlign(toTextAlign(textAnchor))
        myContext.setTextBaseline(toTextBaseline(textDy))
        myContext.fillText(text, x, y)
        restore()
    }

    private fun toTextAlign(textAnchor: String?): Context2d.TextAlign {
        return when (textAnchor) {
            null -> Context2d.TextAlign.START
            "middle" -> Context2d.TextAlign.CENTER
            "end" -> Context2d.TextAlign.END
            else -> throw IllegalArgumentException("Unsupported 'text-anchor' value $textAnchor")
        }
    }

    private fun toTextBaseline(textDy: String?): Context2d.TextBaseline {
        return when (textDy) {
            "1.4em" -> Context2d.TextBaseline.TOP
            "0.7em" -> Context2d.TextBaseline.TOP
            "0.35em" -> Context2d.TextBaseline.MIDDLE
            null -> Context2d.TextBaseline.BOTTOM
            else -> throw IllegalArgumentException("Unsupported 'dy' value $textDy")
        }
    }

    private fun applyTransform(transforms: List<Result>) {
        for (t in transforms) {
            when (t.name) {
                SvgTransform.SCALE -> myContext.scale(t.getParam(SCALE_X)!!, if (t.containsParam(SCALE_Y)) t.getParam(SCALE_Y)!! else t.getParam(SCALE_X)!!)
                SvgTransform.SKEW_X -> myContext.transform(1.0, 0.0, tan(toRadians(t.getParam(SKEW_X_ANGLE)!!)), 1.0, 0.0, 0.0)
                SvgTransform.SKEW_Y -> myContext.transform(1.0, tan(toRadians(t.getParam(SKEW_Y_ANGLE)!!)), 0.0, 1.0, 0.0, 0.0)
                SvgTransform.ROTATE -> {
                    if (t.paramCount == 3) {
                        myContext.translate(t.getParam(ROTATE_X)!!, t.getParam(ROTATE_Y)!!)
                    }
                    myContext.rotate(toRadians(t.getParam(ROTATE_ANGLE)!!))
                }
                SvgTransform.TRANSLATE -> myContext.translate(t.getParam(TRANSLATE_X)!!, if (t.containsParam(TRANSLATE_Y)) t.getParam(TRANSLATE_Y)!! else 0.0)
                SvgTransform.MATRIX -> myContext.transform(
                        t.getParam(MATRIX_11)!!, t.getParam(MATRIX_12)!!,
                        t.getParam(MATRIX_21)!!, t.getParam(MATRIX_22)!!,
                        t.getParam(MATRIX_DX)!!, t.getParam(MATRIX_DY)!!
                )
                else -> throw IllegalArgumentException("Unknown transform: " + t.name)
            }
        }
    }

    companion object {
        private const val DEFAULT_FONT = "15px arial"
        private const val SCALE_X = 0
        private const val SCALE_Y = 1
        private const val SKEW_X_ANGLE = 0
        private const val SKEW_Y_ANGLE = 0
        private const val ROTATE_ANGLE = 0
        private const val ROTATE_X = 1
        private const val ROTATE_Y = 2
        private const val TRANSLATE_X = 0
        private const val TRANSLATE_Y = 1
        private const val MATRIX_11 = 0
        private const val MATRIX_12 = 1
        private const val MATRIX_21 = 2
        private const val MATRIX_22 = 3
        private const val MATRIX_DX = 4
        private const val MATRIX_DY = 5
    }
}
