package com.octopus.utils

fun getFileName(filePath: String): String {
    return filePath.replace("\\", "/").substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."))
}

fun getFileExtension(filePath: String): String {
    return filePath.replace("\\", "/").substring(filePath.lastIndexOf(".") + 1)
}