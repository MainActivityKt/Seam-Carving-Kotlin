package org.example.utils

enum class Prompt(val text: String) {
    ENTER_WIDTH("Enter rectangle width:"),
    ENTER_HEIGHT("Enter rectangle height:"),
    ENTER_NAME("Enter output image name:")
}

fun String.extractArgs(): Pair<String, String> {
    val args = Regex("\\w+-?\\w+.png").findAll(this)
    return args.first().value to args.last().value
}
