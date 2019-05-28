package jetbrains.datalore.visualization.plot.base.scale.transform

import kotlin.math.sqrt

class SqrtTransform : FunTransform(F, F_INVERSE) {
    companion object {
        private val F: (Double?) -> Double? = { v ->
            if (v != null)
                sqrt(v)
            else
                null
        }
        private val F_INVERSE: (Double?) -> Double? = { v ->
            if (v != null)
                v * v
            else
                null
        }
    }
}