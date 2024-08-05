package seamcarving

import utils.Prompt
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class RectangleBuilder {
    private var width: Int = 0
    private var height: Int = 0
    private lateinit var image: BufferedImage

    fun getDetails() {
        println(Prompt.ENTER_WIDTH.text)
        width = readln().toInt()
        println(Prompt.ENTER_HEIGHT.text)
        height = readln().toInt()
    }

    fun createImage() {
        image = BufferedImage(width, height, BufferedImage.TYPE_INT_BGR)
    }

    fun drawRectangles() {
        image.createGraphics().apply {
            color = Color.red
            drawLine(0, 0, image.width - 1, image.height - 1)
            drawLine(0, image.height - 1, image.width - 1, 0)
        }
    }

    fun saveImage() {
        println(Prompt.ENTER_NAME.text)
        val name = readln()
        val outputFile = File(name)
        ImageIO.write(image, "png", outputFile)
    }
}

fun main() {
    val image = RectangleBuilder()
    image.getDetails()
    image.createImage()
    image.drawRectangles()
    image.saveImage()
}