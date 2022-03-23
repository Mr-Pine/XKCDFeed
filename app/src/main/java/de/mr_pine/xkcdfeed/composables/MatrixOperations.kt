package de.mr_pine.xkcdfeed.composables

import kotlin.math.cos
import kotlin.math.sin

fun Array<FloatArray>.matrixMultiply(other: Array<FloatArray>): Array<FloatArray> {
    if (this[0].size != other.size) throw Exception("Matrices not compatible")
    val result = Array(this.size) { FloatArray(other[0].size) }
    for (y in this.indices) {
        for (x in other[0].indices) {
            var sum = 0f
            for (i in this[0].indices) {
                sum += this[y][i] * other[i][x]
            }

            result[y][x] = sum
        }
    }

    return result
}

fun Array<FloatArray>.matrixAdd(other: Array<FloatArray>): Array<FloatArray> {
    if (this.size != other.size || this[0].size != other[0].size) throw Exception("Matrices not compatible")
    val result = Array(this.size) { FloatArray(this.size) }
    for (y in this.indices) {
        for (x in this[0].indices) {
            result[y][x] = this[y][x] + other[y][x]
        }
    }

    return result
}

fun identityMatrix(width: Int, height: Int) =
    Array(height) { index -> FloatArray(width) { indexInner -> if (index == indexInner) 1f else 0f } }

fun xRotation(cos: Float, sin: Float): Array<FloatArray> {
    return arrayOf(
        floatArrayOf(1f, 0f, 0f, 0f),
        floatArrayOf(0f, cos, sin, 0f),
        floatArrayOf(0f, -sin, cos, 0f),
        floatArrayOf(0f, 0f, 0f, 1f)
    )
}

fun xRotation(angle: Float) = xRotation(cos = cos(angle), sin = sin(angle))

fun yRotation(cos: Float, sin: Float): Array<FloatArray> {
    return arrayOf(
        floatArrayOf(cos, 0f, -sin, 0f),
        floatArrayOf(0f, 1f, 0f, 0f),
        floatArrayOf(sin, 0f, cos, 0f),
        floatArrayOf(0f, 0f, 0f, 1f)
    )
}

fun yRotation(angle: Float) = yRotation(cos = cos(angle), sin = sin(angle))

fun zRotation(cos: Float, sin: Float): Array<FloatArray> {
    return arrayOf(
        floatArrayOf(cos, sin, 0f, 0f),
        floatArrayOf(-sin, cos, 0f, 0f),
        floatArrayOf(0f, 0f, 1f, 0f),
        floatArrayOf(0f, 0f, 0f, 1f)
    )
}

fun zRotation(angle: Float) = zRotation(cos = cos(angle), sin = sin(angle))

fun Array<FloatArray>.toColorMatrix(): FloatArray { //for conversion from http://www.graficaobscura.com/matrix/index.html
    if (this[0].size != 4 || this.size != 4) throw Exception("Conversion not possible")
    return floatArrayOf(
        this[0][0], this[1][0], this[2][0], 0f, this[3][0],
        this[0][1], this[1][1], this[2][1], 0f, this[3][1],
        this[0][2], this[1][2], this[2][2], 0f, this[3][2],
        0f, 0f, 0f, 1f, 0f
    )
}

fun Array<FloatArray>.matrixToString(): String {
    return this.joinToString("\n") { it.joinToString(", ") }
}

fun shearZ(x: Float, y: Float): Array<FloatArray> {
    return arrayOf(
        floatArrayOf(1f, 0f,  x, 0f),
        floatArrayOf(0f, 1f,  y, 0f),
        floatArrayOf(0f, 0f, 1f, 0f),
        floatArrayOf(0f, 0f, 0f, 1f),
    )
}

fun Array<FloatArray>.cutTo(width: Int, height: Int): Array<FloatArray> {
    return this.sliceArray(0 until height).map { it.sliceArray(0 until width) }.toTypedArray()
}
