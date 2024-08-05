package seamcarving

import utils.Utils.openImage
import utils.Utils.saveImage
import utils.Parameters
import java.awt.Color
import java.awt.image.BufferedImage

class ColorInverter(inputPath: String) {
    private lateinit var inputImage: BufferedImage

    init {
        inputImage = openImage(inputPath)
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

    fun save(path: String) {
        saveImage(path, inputImage)
    }

    private fun invertRgb(rgb: Color): Color {
        return Color(255 - rgb.red, 255 - rgb.green, 255 - rgb.blue)
    }
}

fun main(args: Array<String>) {
    val params = Parameters(args[1], args.last())
    val colorInverter = ColorInverter(params.inputPath)
    colorInverter.convertImage()
    colorInverter.save(params.outputPath)
}