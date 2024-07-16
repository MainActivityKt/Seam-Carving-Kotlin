package org.example.seamcarving

import org.example.utils.extractArgs
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ColorInverter(inputPath: String) {
    private lateinit var inputImage: BufferedImage

    init {
        openImage(inputPath)
    }

    private fun openImage(inputPath: String) {
        println(inputPath)
       inputImage = ImageIO.read(File(inputPath))
    }

    fun convertImage() {
        for (x in 0 until inputImage.width) {
            for (y in 0 until inputImage.height) {
                val currentRgb = Color(inputImage.getRGB(x, y))
                val invertedRgb = invertRgb(currentRgb)
                inputImage.setRGB(x, y, invertedRgb.rgb)
            }
        }
    }

    fun saveImage(outputPath: String) {
        val outputDir = File(outputPath)
        ImageIO.write(inputImage, "png", outputDir)
    }

    private fun invertRgb(rgb: Color): Color {
        return Color(255 - rgb.red, 255 - rgb.green, 255 - rgb.blue)
    }
}

fun main(args: Array<String>) {
    val arguments = args.joinToString().extractArgs()
    val inverter = ColorInverter(arguments.first)
    inverter.convertImage()
    inverter.saveImage(arguments.second)
}