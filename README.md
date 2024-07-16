# Seam Carving

Seam carving is an image processing technique for content aware image resizing. 

## Stage 1/6:Create an image:
In this stage, a rectangle image of provided width and height is created, with a red rectangle drawn diagonally
on both sides.

[Open stage 1 on Hyperskill](https://hyperskill.org/projects/100/stages/550/implement)

Stage implementation: [RectangleBuilder.kt](src/main/kotlin/seamcarving/RectangleBuilder.kt)

Example

    Enter rectangle width:
    > 20
    Enter rectangle height:
    > 10
    Enter output image name:
    > out.png

out.png looks like this:

![produced image](src/drawable/rect.png)

## Stage 2/6:Negative photo:

In this stage, our Kotlin program converts an image into a negative one, by
inverting its rgb values.

Inverted color for `(r, g, b)` is `(255 - r, 255 - g, 255 - b)`



[Open stage 2 on Hyperskill](https://hyperskill.org/projects/100/stages/551/implement#comment)

Stage implementation:  [ColorInverter.kt](src/main/kotlin/seamcarving/ColorInverter.kt)

Args: `-in path\imageName.png -out outputPath_outputName.png`

Example:

>args: `-in sky.png -out sky_negative.png`
> 
>For the following sky.png:
> 
> ![sky image](src/drawable/sky.png)
> 
> sky-negative.png looks like:
> 
> ![inverted sky image](src/drawable/sky_negative.png)