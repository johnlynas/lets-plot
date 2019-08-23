package jetbrains.datalore.visualization.plot.builder.tooltip

import jetbrains.datalore.base.geometry.DoubleRectangle
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.base.values.Colors
import jetbrains.datalore.visualization.base.svg.SvgPathDataBuilder
import jetbrains.datalore.visualization.base.svg.SvgPathElement
import jetbrains.datalore.visualization.base.svg.SvgSvgElement
import jetbrains.datalore.visualization.plot.base.render.svg.SvgComponent
import jetbrains.datalore.visualization.plot.base.render.svg.TextLabel
import jetbrains.datalore.visualization.plot.builder.interact.render.Orientation
import jetbrains.datalore.visualization.plot.builder.interact.render.TooltipViewModel
import jetbrains.datalore.visualization.plot.builder.presentation.Defaults.Common.Tooltip.DARK_TEXT_COLOR
import jetbrains.datalore.visualization.plot.builder.presentation.Defaults.Common.Tooltip.H_CONTENT_PADDING
import jetbrains.datalore.visualization.plot.builder.presentation.Defaults.Common.Tooltip.LIGHT_TEXT_COLOR
import jetbrains.datalore.visualization.plot.builder.presentation.Defaults.Common.Tooltip.LINE_INTERVAL
import jetbrains.datalore.visualization.plot.builder.presentation.Defaults.Common.Tooltip.MAX_POINTER_FOOTING_LENGTH
import jetbrains.datalore.visualization.plot.builder.presentation.Defaults.Common.Tooltip.POINTER_FOOTING_TO_SIDE_LENGTH_RATIO
import jetbrains.datalore.visualization.plot.builder.presentation.Defaults.Common.Tooltip.V_CONTENT_PADDING
import jetbrains.datalore.visualization.plot.builder.tooltip.TooltipBox.PointerDirection.*
import kotlin.math.max
import kotlin.math.min

class TooltipBox : SvgComponent() {
    private enum class PointerDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    val contentRect get() = DoubleRectangle.span(DoubleVector.ZERO, myTextBox.dimension)
    private val myPointerBox = PointerBox()
    private val myTextBox = TextBox()

    private var textColor: Color = Color.BLACK
    private var fillColor: Color = Color.WHITE
    private var pointerDirection: PointerDirection? = null
    private lateinit var pointerCoord: DoubleVector

    override fun buildComponent() {
        add(myPointerBox)
        add(myTextBox)
    }

    internal fun setViewModel(viewModel: TooltipViewModel) {
        setContent(viewModel.fill, viewModel.text, viewModel.style)
    }

    internal fun setContent(background: Color, lines: List<String>, style: String) {
        addClassName(style)
        fillColor = Colors.mimicTransparency(background, background.alpha / 255.0, Color.WHITE)
        textColor = LIGHT_TEXT_COLOR.takeIf { fillColor.isDark() } ?: DARK_TEXT_COLOR

        myTextBox.update(lines, textColor)
    }

    fun setPosition(contentCoord: DoubleVector, pointerCoord: DoubleVector, orientation: Orientation) {
        this.pointerCoord = pointerCoord.subtract(contentCoord)
        pointerDirection =
            null.takeIf { contentRect.contains(this.pointerCoord) }
                ?: run {
                    val tooltip = DoubleRectangle(contentCoord, contentRect.dimension)
                    when (orientation) {
                        Orientation.HORIZONTAL -> RIGHT.takeIf { pointerCoord.x > tooltip.right } ?: LEFT
                        Orientation.VERTICAL -> UP.takeIf { pointerCoord.y < tooltip.bottom } ?: DOWN
                    }
                }

        myPointerBox.update()
        moveTo(contentCoord.x, contentCoord.y)
    }

    private fun Color.isDark() = Colors.luminance(this) < 0.5

    inner class PointerBox : SvgComponent() {
        private val myPointerPath = SvgPathElement()

        override fun buildComponent() {
            add(myPointerPath)
        }

        internal fun update() {
            myPointerPath.strokeColor().set(textColor)
            myPointerPath.fillColor().set(fillColor)

            val vertFootingIndent = -calculatePointerFootingIndent(contentRect.height)
            val horFootingIndent = calculatePointerFootingIndent(contentRect.width)

            myPointerPath.d().set(
                SvgPathDataBuilder().apply {
                    with(contentRect) {

                        fun lineToIf(p: DoubleVector, isTrue: Boolean) { if (isTrue) lineTo(p) }

                        // start point
                        moveTo(right, bottom)

                        // right side
                        lineTo(right, bottom + vertFootingIndent)
                        lineToIf(pointerCoord, pointerDirection == RIGHT)
                        lineTo(right, top - vertFootingIndent)
                        lineTo(right, top)

                        // top side
                        lineTo(right - horFootingIndent, top)
                        lineToIf (pointerCoord, pointerDirection == UP)
                        lineTo(left + horFootingIndent, top)
                        lineTo(left, top)

                        // left side
                        lineTo(left, top - vertFootingIndent)
                        lineToIf (pointerCoord, pointerDirection == LEFT)
                        lineTo(left, bottom + vertFootingIndent)
                        lineTo(left, bottom)

                        // bottom
                        lineTo(left + horFootingIndent, bottom)
                        lineToIf (pointerCoord, pointerDirection == DOWN)
                        lineTo(right - horFootingIndent, bottom)
                        lineTo(right, bottom)
                    }
                }.build()
            )
        }

        private fun calculatePointerFootingIndent(sideLength: Double): Double {
            val footingLength = min(sideLength * POINTER_FOOTING_TO_SIDE_LENGTH_RATIO, MAX_POINTER_FOOTING_LENGTH)
            return (sideLength - footingLength) / 2
        }
    }

    inner class TextBox : SvgComponent() {
        private val myLines = SvgSvgElement().apply {
            x().set(0.0)
            y().set(0.0)
            width().set(0.0)
            height().set(0.0)
        }
        private val myContent = SvgSvgElement().apply {
            x().set(0.0)
            y().set(0.0)
            width().set(0.0)
            height().set(0.0)
        }

        val dimension get() = myContent.run { DoubleVector(width().get()!!, height().get()!!) }

        override fun buildComponent() {
            myContent.children().add(myLines)
            add(myContent)
        }

        internal fun update(lines: List<String>, textColor: Color) {
            val textSize = lines
                .map { TextLabel(it).apply { textColor().set(textColor) } }
                .onEach { myLines.children().add(it.rootGroup) }
                .fold(DoubleVector.ZERO, { textDimension, label ->
                    val labelBbox = label.rootGroup.bBox

                    // bBox.top is negative baseline of the text.
                    // Can't use bBox.height:
                    //  - in Batik it is close to the abs(bBox.top)
                    //  - in JavaFx it is constant = fontSize
                    label.y().set(textDimension.y - labelBbox.top)

                    // Again works differently in Batik(some positive padding) and JavaFX (always zero)
                    label.x().set(-labelBbox.left)

                    DoubleVector(
                        max(textDimension.x, labelBbox.width),
                        label.y().get()!! + LINE_INTERVAL
                    )
                })
                .subtract(DoubleVector(0.0, LINE_INTERVAL)) // remove LINE_INTERVAL from last line

            myLines.apply {
                x().set(H_CONTENT_PADDING)
                y().set(V_CONTENT_PADDING)
                width().set(textSize.x)
                height().set(textSize.y)
            }

            myContent.apply {
                width().set(textSize.x + H_CONTENT_PADDING * 2)
                height().set(textSize.y + V_CONTENT_PADDING * 2)
            }
        }
    }
}

