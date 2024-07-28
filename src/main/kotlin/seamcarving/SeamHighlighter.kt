package org.example.seamcarving

import org.example.utils.*
import org.example.utils.Utils.openImage
import org.example.utils.Utils.saveImage
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.sqrt

class SeamHighlighter(inputPath: String) {
    private val inputImage: BufferedImage
    private var energies: MutableList<MutableList<Double>>
    private var cumulativeEnergies: MutableList<MutableList<Double>>
    private var path = mutableListOf<Pixel>()
    private val height: Int
    private val width: Int

    init {
        inputImage = openImage(inputPath)
        height = inputImage.height
        width = inputImage.width
        energies = MutableList(width) { MutableList(height) { 0.0 } }
        cumulativeEnergies = MutableList(height) { MutableList(width) { 0.0 } }
    }

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

    fun calculateCumulativeEnergies() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (y == 0) {
                    // first has no row above it, so will be initialized with energy data
                    cumulativeEnergies[y][x] = energies[x][y]
                } else {
                    cumulativeEnergies[y][x] = energies[x][y] + getMinEnergy(x, y)
                }
            }
        }
    }

    fun findLowestSeam() {
        val x = cumulativeEnergies[height - 1].indexOf(cumulativeEnergies[height - 1].min())
        path = getPath(x, height - 1)
    }

    private fun getPath(startX: Int, startY: Int): MutableList<Pixel> {
        var currentPixel = Pixel(startX, startY)
        val path = mutableListOf(currentPixel)
        for (y in height - 1 downTo 1) {
            currentPixel = findNextBestPixel(Pixel(currentPixel.x, currentPixel.y))
            path.add(currentPixel)
        }
        return path
    }

    private fun findNextBestPixel(pixel: Pixel): Pixel {
        val top = Pixel(pixel.x, pixel.y - 1) to cumulativeEnergies[pixel.y - 1][pixel.x]
        val topLeft = if (pixel.x > 0) Pixel(pixel.x - 1, pixel.y - 1) to cumulativeEnergies[pixel.y - 1][pixel.x - 1] else top
        val topRight = if (pixel.x < width - 1) Pixel(pixel.x + 1, pixel.y - 1) to cumulativeEnergies[pixel.y - 1][pixel.x + 1] else top
        return setOf(top, topLeft, topRight).minBy { it.second }.first
    }

    private fun getMinEnergy(x: Int, y: Int): Double {
        val top = cumulativeEnergies[y - 1][x]
        val topLeft = if (x > 0) cumulativeEnergies[y - 1][x - 1] else top
        val topRight = if (x < width - 1) cumulativeEnergies[y - 1][x + 1] else top
        return minOf(top, topLeft, topRight)
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
    val arguments = args.joinToString().extractArgs()
    val seamHighlighter = SeamHighlighter(arguments.first)
    seamHighlighter.calculateEnergies()
    seamHighlighter.calculateCumulativeEnergies()
    seamHighlighter.findLowestSeam()
    seamHighlighter.colorizeSeam(Color.RED)
    seamHighlighter.save(arguments.second)
}