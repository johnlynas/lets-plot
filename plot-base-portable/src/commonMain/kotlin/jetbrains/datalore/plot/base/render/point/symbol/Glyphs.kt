/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.render.point.symbol

import jetbrains.datalore.base.geometry.DoubleVector

object Glyphs {
    fun square(location: DoubleVector, width: Double): Glyph {
        return SquareGlyph(location, width)
    }

    fun circle(location: DoubleVector, width: Double): Glyph {
        return CircleGlyph(location, width)
    }

    fun diamond(location: DoubleVector, width: Double): Glyph {
        return DiamondGlyph(location, width)
    }

    fun triangleUp(location: DoubleVector, width: Double): Glyph {
        return TriangleGlyph(location, width, true)
    }

    fun triangleDown(location: DoubleVector, width: Double): Glyph {
        return TriangleGlyph(location, width, false)
    }

    fun stickPlus(location: DoubleVector, width: Double): Glyph {
        return PlusGlyph(location, width)
    }

    fun stickCross(location: DoubleVector, width: Double): Glyph {
        return CrossGlyph(location, width)
    }

    fun stickSquareCross(location: DoubleVector, size: Double): Glyph {
        return GlyphPair(
            SquareGlyph(location, size),
            CrossGlyph(location, size, false)
        )
    }

    fun stickStar(location: DoubleVector, size: Double): Glyph {
        return GlyphPair(
            PlusGlyph(location, size),
            CrossGlyph(location, size)
        )
    }

    fun stickDiamondPlus(location: DoubleVector, size: Double): Glyph {
        return GlyphPair(
            DiamondGlyph(location, size),
            PlusGlyph(location, size)
        )
    }

    fun stickCirclePlus(location: DoubleVector, size: Double): Glyph {
        return GlyphPair(
            CircleGlyph(location, size),
            PlusGlyph(location, size)
        )
    }

    fun stickTriangleUpDown(location: DoubleVector, size: Double): Glyph {
        return GlyphPair(
            TriangleGlyph(location, size, true),
            TriangleGlyph(location, size, false)
        )
    }

    fun stickSquarePlus(location: DoubleVector, size: Double): Glyph {
        return GlyphPair(
            SquareGlyph(location, size),
            PlusGlyph(location, size)
        )
    }

    fun stickCircleCross(location: DoubleVector, size: Double): Glyph {
        return GlyphPair(
            CircleGlyph(location, size),
            CrossGlyph(location, size)
        )
    }

    fun stickSquareTriangleUp(location: DoubleVector, size: Double): Glyph {
        return GlyphPair(
            SquareGlyph(location, size),
            TriangleGlyph(
                location,
                size,
                true,
                true
            )
        )
    }
}
