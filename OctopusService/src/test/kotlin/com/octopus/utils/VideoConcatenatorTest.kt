package com.octopus.utils

import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VideoConcatenatorTest {

    companion object {
        const val sourceDir = "src/test/resources/video/concat/parts"
        const val outputFilePath = "src/test/resources/video/concat/out/combined.mov"
    }

    @BeforeTest fun setup() {
         File(outputFilePath).delete()
    }

    @Test fun testRetrieveFilesInFolder() {
        val exitCode = VideoConcatenator.concatVideosInDir(sourceDir, outputFilePath)
        assertEquals(0, exitCode)
    }
}