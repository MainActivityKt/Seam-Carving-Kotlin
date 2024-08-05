package org.example.seamcarving

import org.example.utils.GRADIENT
import org.example.utils.Pixel
import org.example.utils.Utils.openImage
import org.example.utils.Utils.saveImage
import org.example.utils.Parameters
import org.example.utils.squared
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.sqrt

enum class REDUCTION {
    HORIZONTAL_REDUCTION, VERTICAL_REDUCTION
}

class ImageResizer() {

    private lateinit var lowestSeamsPath: MutableList<Pixel>
    private lateinit var imageArray: MutableList<MutableList<Int>>
    private lateinit var outputImage: BufferedImage
    private lateinit var energies: MutableList<MutableList<Double>>
    private lateinit var cumulativeEnergies: MutableList<MutableList<Double>>
    private lateinit var currentReduction: REDUCTION
    private var height = 0
    private var width = 0

    fun resizeImage(image: BufferedImage, reduction: REDUCTION, decrease: Int) {
        currentReduction = reduction
        val inputImage = if (currentReduction == REDUCTION.VERTICAL_REDUCTION) image else transpose(image)
        height = inputImage.height
        width = inputImage.width
        outputImage = BufferedImage(width - decrease, height, BufferedImage.TYPE_INT_RGB)
        imageArray = MutableList(height) { MutableList(width) { 0 } }
        for (y in 0 until inputImage.height) {
            for (x in 0 until inputImage.width) {
                imageArray[y][x] = inputImage.getRGB(x, y)
            }
        }
        lowestSeamsPath = mutableListOf()

        repeat(decrease) {
            calculateEnergies()
            calculateCumulativeEnergies()
            findLowestSeam()
            removeLowestSeam()
        }
    }

    private fun calculateEnergies() {
        energies = MutableList(width) { MutableList(height) { 0.0 } }
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
        val prevPixelColor = Color(imageArray[prevPixel.y][prevPixel.x])
        val nextPixelColor = Color(imageArray[nextPixel.y][nextPixel.x])

        val redDiff = prevPixelColor.red - nextPixelColor.red
        val greenDiff = prevPixelColor.green - nextPixelColor.green
        val blueDiff = prevPixelColor.blue - nextPixelColor.blue

        return redDiff.squared() + greenDiff.squared() + blueDiff.squared()
    }

    private fun calculateCumulativeEnergies() {
        cumulativeEnergies = MutableList(height) { MutableList(width) { 0.0 } }
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (y == 0) {
                    // The top row has nothing above it, so the energies are the same as the source image
                    cumulativeEnergies[y][x] = energies[x][y]
                } else {
                    // For each pixel in the rest of the rows, the energy is its own energy plus the minimal of the three energies above.
                    cumulativeEnergies[y][x] = energies[x][y] + getNextPixel(x, y).energy
                }
            }
        }
    }

    private fun findLowestSeam() {
        val x = cumulativeEnergies[height - 1].indexOf(cumulativeEnergies[height - 1].min())
        lowestSeamsPath = getPath(x, height - 1)
    }

    private fun removeLowestSeam() {
        lowestSeamsPath.forEach { p ->
            imageArray[p.y].removeAt(p.x)
        }
        width -= 1
    }

    private fun getPath(startX: Int, startY: Int): MutableList<Pixel> {
        var currentPixel = Pixel(startX, startY)
        val path = mutableListOf(currentPixel)
        for (y in height - 1 downTo 1) {
            currentPixel = getNextPixel(currentPixel.x, currentPixel.y)
            path.add(currentPixel)
        }
        return path
    }

     private fun getNextPixel(x: Int, y: Int): Pixel {
        val top = Pixel(x, y - 1, cumulativeEnergies[y - 1][x])
        val topLeft = if (x > 0) Pixel(x - 1, y - 1, cumulativeEnergies[y - 1][x - 1]) else top
        val topRight = if (x < width - 1) Pixel(x + 1, y - 1, cumulativeEnergies[y - 1][x + 1]) else top
        return setOf(top, topLeft, topRight).minBy { it.energy }
    }

    fun getOutputImage(): BufferedImage {
        for (x in 0 until width) {
            for (y in 0 until height) {
                outputImage.setRGB(x, y, imageArray[y][x])
            }
        }
        return if (currentReduction == REDUCTION.VERTICAL_REDUCTION) outputImage else transpose(outputImage)
    }
}

private fun transpose(image: BufferedImage): BufferedImage {
    val transposedImage = BufferedImage(image.height, image.width, image.type)
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            transposedImage.setRGB(y, x, image.getRGB(x, y))
        }
    }
    return transposedImage
}

fun main(args: Array<String>) {
    val params = Parameters(args[1], args[3], args[5].toInt(), args.last().toInt())

    var image = openImage(params.inputPath)
    ImageResizer().apply {
        resizeImage(image, REDUCTION.VERTICAL_REDUCTION, params.width)
        image = getOutputImage()
        resizeImage(image, REDUCTION.HORIZONTAL_REDUCTION, params.height)
        image = getOutputImage()
    }
    saveImage(params.outputPath, image)
}