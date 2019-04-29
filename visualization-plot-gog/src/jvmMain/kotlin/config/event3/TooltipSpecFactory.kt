package jetbrains.datalore.visualization.plot.gog.config.event3

import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.values.Pair
import jetbrains.datalore.visualization.plot.gog.core.event.MappedDataAccess
import jetbrains.datalore.visualization.plot.gog.core.event.MappedDataAccess.MappedData
import jetbrains.datalore.visualization.plot.gog.core.event3.ContextualMapping
import jetbrains.datalore.visualization.plot.gog.core.event3.GeomTarget
import jetbrains.datalore.visualization.plot.gog.core.event3.TipLayoutHint
import jetbrains.datalore.visualization.plot.gog.core.render.Aes
import jetbrains.datalore.visualization.plot.gog.plot.event3.tooltip.TooltipSpec

class TooltipSpecFactory(contextualMapping: ContextualMapping, private val myAxisOrigin: DoubleVector) {
    private val myTooltipAes: List<Aes<*>> = contextualMapping.tooltipAes
    private val myAxisAes: List<Aes<*>> = contextualMapping.axisAes
    private val myDataAccess: MappedDataAccess = contextualMapping.dataAccess

    fun create(geomTarget: GeomTarget): List<TooltipSpec> {
        return TargetTootipSpecBuilder(geomTarget).createTooltipSpecs()
    }

    private inner class TargetTootipSpecBuilder internal constructor(private val myGeomTarget: GeomTarget) {
        private val myTooltipSpecs = ArrayList<TooltipSpec>()
        private val myShortLabels = ArrayList<String>()
        private val myAesWithoutHint = ArrayList(myTooltipAes)

        init {

            listOf(Aes.X, Aes.Y)
                    .forEach { aes ->
                        if (isMapped(aes)) {
                            myShortLabels.add(getMappedData(aes).label)
                        }
                    }
        }

        internal fun createTooltipSpecs(): List<TooltipSpec> {
            aesTipLayoutHints().forEach { (aes, hint) -> addHintTooltipSpec(listOf(aes), hint) }

            addAesTooltipSpec()

            addAxisTooltipSpec()

            return myTooltipSpecs
        }

        private fun hitIndex(): Int {
            return myGeomTarget.hitIndex
        }

        private fun tipLayoutHint(): TipLayoutHint {
            return myGeomTarget.tipLayoutHint
        }

        private fun aesTipLayoutHints(): Map<Aes<*>, TipLayoutHint> {
            return myGeomTarget.aesTipLayoutHints
        }

        private fun addAxisTooltipSpec() {
            for (aes in myAxisAes) {
                if (isAxisTooltipAllowed(aes)) {
                    val layoutHint = createHintForAxis(aes)
                    val text = makeText(listOf(aes))
                    myTooltipSpecs.add(TooltipSpec(layoutHint, text, layoutHint.color))
                }
            }
        }

        private fun isAxisTooltipAllowed(aes: Aes<*>): Boolean {
            if (!isMapped(aes)) {
                return false
            }

            val label = getMappedData(aes).label
            return if (MAP_COORDINATE_NAMES.contains(label)) {
                false
            } else isVariableContinuous(aes)

        }

        private fun addAesTooltipSpec() {
            val aesListForTooltip = ArrayList(myAesWithoutHint)

            removeAxisMapping(aesListForTooltip)
            removeAutoGeneratedMappings(aesListForTooltip)
            removeMapIdMapping(aesListForTooltip)
            removeDiscreteDuplicatedMappings(aesListForTooltip)

            addHintTooltipSpec(aesListForTooltip, tipLayoutHint())
        }

        private fun removeAxisMapping(aesListForTooltip: MutableList<Aes<*>>) {
            for (axisAes in myAxisAes) {
                if (isVariableContinuous(axisAes)) {
                    val axisTooltipLabel = getMappedData(axisAes).label
                    aesListForTooltip.removeIf { aes -> getMappedData(aes).label.equals(axisTooltipLabel) }
                }
            }
        }

        private fun removeDiscreteDuplicatedMappings(aesWithoutHint: MutableList<Aes<*>>) {
            if (aesWithoutHint.isEmpty()) {
                return
            }

            val mappingsToShow = HashMap<String, Pair<Aes<*>, MappedData<*>>>()
            for (aes in aesWithoutHint) {
                if (!isMapped(aes)) {
                    continue
                }

                val mappingToCheck = getMappedData(aes)
                if (!mappingsToShow.containsKey(mappingToCheck.label)) {
                    mappingsToShow[mappingToCheck.label] = Pair<Aes<*>, MappedData<*>>(aes, mappingToCheck)
                    continue
                }

                val mappingToShow = mappingsToShow[mappingToCheck.label]?.second
                if (!mappingToShow!!.isContinuous && mappingToCheck.isContinuous) {
                    mappingsToShow[mappingToCheck.label] = Pair(aes, mappingToCheck)
                }
            }

            aesWithoutHint.clear()
            mappingsToShow.values.forEach { pair -> aesWithoutHint.add(pair.first!!) }
        }

        private fun removeAutoGeneratedMappings(aesListForTooltip: MutableList<Aes<*>>) {
            aesListForTooltip.removeIf { mapping -> AUTO_GENERATED_NAMES.contains(getMappedData(mapping).label) }
        }

        private fun removeMapIdMapping(aesListForTooltip: MutableList<Aes<*>>) {
            aesListForTooltip.removeIf { mapping -> mapping === Aes.MAP_ID }
        }

        private fun isVariableContinuous(aes: Aes<*>): Boolean {
            if (!isMapped(aes)) {
                return false
            }

            val xData = getMappedData(aes)
            return xData.isContinuous
        }

        private fun createHintForAxis(aes: Aes<*>): TipLayoutHint {
            if (aes === Aes.X) {
                return TipLayoutHint.xAxisTooltip(DoubleVector(tipLayoutHint().coord.x, myAxisOrigin.y), GeomTargetInteraction.AXIS_TOOLTIP_COLOR)
            }

            if (aes === Aes.Y) {
                return TipLayoutHint.yAxisTooltip(DoubleVector(myAxisOrigin.x, tipLayoutHint().coord.y), GeomTargetInteraction.AXIS_TOOLTIP_COLOR)
            }

            throw IllegalArgumentException("Not an axis aes: $aes")
        }

        private fun addHintTooltipSpec(aes: List<Aes<*>>, layoutHint: TipLayoutHint) {
            if (aes.isEmpty()) {
                return
            }

            val text = makeText(aes)
            val fill = layoutHint.color ?: tipLayoutHint().color
            myTooltipSpecs.add(
                    TooltipSpec(layoutHint, text, fill)
            )
            myAesWithoutHint.removeAll(aes)
        }

        private fun format(mappedData: MappedData<*>): String {
            if (mappedData.label.isEmpty()) {
                return mappedData.value
            }

            return if (myShortLabels.contains(mappedData.label)) {
                mappedData.value
            } else mappedData.label + ": " + mappedData.value

        }

        private fun makeText(aesList: List<Aes<*>>): List<String> {
            val lines = ArrayList<String>()

            for (aes in aesList) {
                if (isMapped(aes)) {
                    val mappedData = getMappedData(aes)
                    val string = format(mappedData)
                    if (!lines.contains(string)) {
                        lines.add(string)
                    }
                }
            }

            return lines
        }

        private fun isMapped(aes: Aes<*>): Boolean {
            return myDataAccess.isMapped(aes)
        }

        private fun <T> getMappedData(aes: Aes<T>): MappedData<T> {
            return myDataAccess.getMappedData(aes, hitIndex())
        }
    }

    companion object {
        private val MAP_COORDINATE_NAMES = setOf("lon", "longitude", "lat", "latitude")
        private val AUTO_GENERATED_NAMES = setOf<String>()
    }
}
