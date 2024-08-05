package seamcarving

import utils.GRADIENT
import utils.Pixel
import utils.Utils.openImage
import utils.Utils.saveImage
import utils.squared
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.sqrt

open class SeamHighlighter(inputPath: String) {
    protected val inputImage: BufferedImage
    protected var energies: MutableList<MutableList<Double>>
    protected var path = mutableListOf<Pixel>()
    protected val height: Int
    protected val width: Int
    private var cumulativeEnergies: MutableList<MutableList<Double>>

    init {
        inputImage = openImage(inputPath)
        height = inputImage.height
        width = inputImage.width
        energies = MutableList(width) { MutableList(height) { 0.0 } }             // initialize list of pixel energies
        cumulativeEnergies = MutableList(height) { MutableList(width) { 0.0 } }  // initialize list of cumulative energies
    }

    /**
     * Calculates the energy of each pixel, and adds that energy to the energies 2D array, for that pixel
     * @see calculatePixelEnergy
     */
    fun calculateEnergies() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                val xDiff = calculatePixelEnergy(x, y, GRADIENT.X_GRADIENT)
                val yDiff = calculatePixelEnergy(x, y, GRADIENT.Y_GRADIENT)
                energies[x][y] = sqrt(((xDiff + yDiff).toDouble()))
            }
        }
    }

    private fun calculatePixelEnergy(x: Int, y: Int, gradient: GRADIENT): Int {
        val prevPixel: Pixel
        val nextPixel: Pixel
        val currentPixel: Pixel
        if (gradient == GRADIENT.X_GRADIENT) {
            currentPixel = Pixel(if (x == 0) 1 else if (x == width - 1) width - 2 else x, y)
            prevPixel = Pixel(currentPixel.x - 1, y)
            nextPixel = Pixel(currentPixel.x + 1, y)
        } else {
            currentPixel = Pixel(x, if (y == 0) 1 else if (y == height - 1) height - 2 else y)
            prevPixel = Pixel(x, currentPixel.y - 1)
            nextPixel = Pixel(x, currentPixel.y + 1)
        }
        return getRgbDiff(prevPixel, nextPixel)
    }

    private fun getRgbDiff(prevPixel: Pixel, nextPixel: Pixel): Int {
        val prevPixelColor = Color(inputImage.getRGB(prevPixel.x, prevPixel.y))
        val nextPixelColor = Color(inputImage.getRGB(nextPixel.x, nextPixel.y))

        val redDiff = prevPixelColor.red - nextPixelColor.red
        val greenDiff = prevPixelColor.green - nextPixelColor.green
        val blueDiff = prevPixelColor.blue - nextPixelColor.blue

        return redDiff.squared() + greenDiff.squared() + blueDiff.squared()
    }

    /**
     * Calculates the cumulative energy of each pixel, and adds that energy to cumulativeEnergies 2D array for that pixel
     * @see calculateEnergies
     */
    open fun calculateCumulativeEnergies() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (y == 0) {
                    // The top row has nothing above it, so the energies are the same as the source image
                    cumulativeEnergies[y][x] = energies[x][y]
                } else {
                    // For each pixel in the rest of the rows,
                    // the energy is its own energy plus the minimal of the three energies above.
                    cumulativeEnergies[y][x] = energies[x][y] + getNextPixel(x, y).energy
                }
            }
        }
    }

    /**
     * Marks the pixel with the lowest cumulative energy value from the bottom row as the starting point of seam path and
     * updates the path variable using getPath() function
     */
    open fun findLowestSeam() {
        val x = cumulativeEnergies[height - 1].indexOf(cumulativeEnergies[height - 1].min())
        path = getPath(x, height - 1)
    }

    /**
    Takes the coordinates [x, y] of the starting pixel, and follows the path always choosing the minimum of top three pixels.

    Returns the list of chosen pixels
     */
    open fun getPath(startX: Int, startY: Int): MutableList<Pixel> {
        var currentPixel = Pixel(startX, startY)
        val path = mutableListOf(currentPixel)
        for (y in height - 1 downTo 1) {
            currentPixel = getNextPixel(currentPixel.x, currentPixel.y)
            path.add(currentPixel)
        }
        return path
    }

    /**
     * Takes a pixel coordinates as input, chooses the minimum of top, top left, and top right pixels based on their
     * cumulative energies, and returns the Pixel of that pixel
     * @see Pixel
     */
    protected open fun getNextPixel(x: Int, y: Int): Pixel {
        val top = Pixel(x, y - 1, cumulativeEnergies[y - 1][x])
        val topLeft = if (x > 0) Pixel(x - 1, y - 1, cumulativeEnergies[y - 1][x - 1]) else top
        val topRight = if (x < width - 1) Pixel(x + 1, y - 1, cumulativeEnergies[y - 1][x + 1]) else top
        return setOf(top, topLeft, topRight).minBy { it.energy }
    }

    fun colorizeSeam(color: Color) {
        path.forEach {
            inputImage.setRGB(it.x, it.y, color.rgb)
        }
    }

    fun save(outputPath: String) {
        saveImage(outputPath, inputImage)
    }
}


fun main(args: Array<String>) {
    val seamHighlighter = SeamHighlighter(args[1])
    seamHighlighter.calculateEnergies()
    seamHighlighter.calculateCumulativeEnergies()
    seamHighlighter.findLowestSeam()
    seamHighlighter.colorizeSeam(Color.RED)
    seamHighlighter.save(args.last())
}