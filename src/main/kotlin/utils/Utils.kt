package org.example.utils

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

enum class Prompt(val text: String) {
    ENTER_WIDTH("Enter rectangle width:"),
    ENTER_HEIGHT("Enter rectangle height:"),
    ENTER_NAME("Enter output image name:")
}

enum class GRADIENT {
    X_GRADIENT, Y_GRADIENT
}

data class Pixel(val x: Int, val y: Int, val energy: Double = 0.0) {
    override fun toString(): String {
        return "x: $x, y: $y"
    }
}

fun String.extractArgs(): Pair<String, String> {
    val args = Regex("\\w+-?\\w+.png").findAll(this)
    return args.first().value to args.last().value
}

fun Int.squared() = this * this

object Utils {
    val openImage = { inputPath: String -> ImageIO.read(File("C:\\Users\\Safiu\\IdeaProjects (Kotlin)\\Seam-Carving\\src\\drawable\\$inputPath")) }
    val saveImage = { outputPath: String, image: BufferedImage -> ImageIO.write(image, "png", File("C:\\Users\\Safiu\\IdeaProjects (Kotlin)\\Seam-Carving\\src\\drawable\\$outputPath")) }
}
