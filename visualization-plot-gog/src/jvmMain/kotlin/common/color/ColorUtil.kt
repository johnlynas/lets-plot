package jetbrains.datalore.visualization.plot.gog.common.color

import jetbrains.datalore.base.values.Color

import java.util.ArrayList

object ColorUtil {
    fun genColors(count: Int, baseColors: List<Color>): List<Color> {
        val rValues = ArrayList<Int>()
        val gValues = ArrayList<Int>()
        val bValues = ArrayList<Int>()
        for (baseColor in baseColors) {
            rValues.add(baseColor.red)
            gValues.add(baseColor.green)
            bValues.add(baseColor.blue)
        }
        val rChannelGen = RGBChannelGen(rValues)
        val gChannelGen = RGBChannelGen(gValues)
        val bChannelGen = RGBChannelGen(bValues)

        val rValuesGen = rChannelGen.generate(count).iterator()
        val gValuesGen = gChannelGen.generate(count).iterator()
        val bValuesGen = bChannelGen.generate(count).iterator()

        val colorsGen = ArrayList<Color>(count)
        while (rValuesGen.hasNext() && gValuesGen.hasNext() && bValuesGen.hasNext()) {
            val colorGen = Color(rValuesGen.next(), gValuesGen.next(), bValuesGen.next())
            colorsGen.add(colorGen)
        }

        return colorsGen
    }
}
