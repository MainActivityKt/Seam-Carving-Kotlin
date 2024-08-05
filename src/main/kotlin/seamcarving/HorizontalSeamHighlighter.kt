package org.example.seamcarving

import org.example.utils.*
import java.awt.Color

class HorizontalSeamHighlighter(inputPath: String): SeamHighlighter(inputPath) {

    private val cumulativeEnergies: MutableList<MutableList<Double>>
    init {
        // initialize list of pixel energies
        cumulativeEnergies = MutableList(width) { MutableList(height) { 0.0 } }  // initialize list of cumulative energies
    }

    /**
     * Calculates the cumulative energy of each pixel, and adds that energy to cumulativeEnergies 2D array for that pixel
     * @see calculateEnergies
     */
    override fun calculateCumulativeEnergies() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (x == 0) {
                    // The first column has nothing before it, so the energies are the same as the source image
                    cumulativeEnergies[x][y] = energies[x][y]
                } else {
                    // For each pixel in the rest of the rows,
                    // the energy is its own energy plus the minimal of the three energies to the left.
                    cumulativeEnergies[x][y] = energies[x][y] + getNextPixel(x, y).energy
                }
            }
        }
    }

    /**
     * Marks the pixel with the lowest cumulative energy value from the bottom row as the starting point of seam path and
     * updates the path variable using getPath() function
     */
    override fun findLowestSeam() {
        val y = cumulativeEnergies[width - 1].indexOf(cumulativeEnergies[width - 1].min())
        path = getPath(width - 1, y)
    }

    /**
    Takes the coordinates [x, y] of the starting pixel, and follows the path always choosing the minimum of three pixels to the left.

    Returns the list of chosen pixels
     */
    override fun getPath(startX: Int, startY: Int): MutableList<Pixel> {
        var currentPixel = Pixel(startX, startY)
        val path = mutableListOf(currentPixel)
        for (x in width - 1 downTo 1) {
            currentPixel = getNextPixel(currentPixel.x, currentPixel.y)
            path.add(currentPixel)
        }
        return path
    }

    override fun getNextPixel(x: Int, y: Int): Pixel {
        val left = Pixel(x - 1, y, cumulativeEnergies[x - 1][y])
        val upperLeft = if(y > 0) Pixel(x - 1, y - 1, cumulativeEnergies[x - 1][y - 1]) else left
        val lowerLeft = if (y < height - 1 )Pixel(x - 1, y + 1, cumulativeEnergies[x - 1][y + 1]) else left
        return setOf(left, upperLeft, lowerLeft).minBy { it.energy }
    }
}


fun main(args: Array<String>) {
    val params = Parameters(args[1], args.last())
    val seamHighlighter = HorizontalSeamHighlighter(params.inputPath)
    seamHighlighter.calculateEnergies()
    seamHighlighter.calculateCumulativeEnergies()
    seamHighlighter.findLowestSeam()
    seamHighlighter.colorizeSeam(Color.RED)
    seamHighlighter.save(params.outputPath)
}