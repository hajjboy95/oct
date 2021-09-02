package com.octopus.utils

import org.apache.commons.io.FileUtils
import java.io.File
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VideoSplitterTest {

    companion object {
        const val outputSource = "src/test/resources/video/out/"
        const val videoSource = "src/test/resources/video/"
    }

    // Delete contents of the output folder
    @BeforeTest fun setup() {
        val outFolder = File(outputSource)
        FileUtils.deleteDirectory(outFolder)
        outFolder.mkdir()
    }

    @Test fun testSplitOfSmallMp4Video() {
        testSplitVideo("star_240p.mp4")
    }

    @Test fun testSplitOfLargeMp4Video() {
        testSplitVideo("star_360p.mp4")
    }

    @Test fun testSplitOfSmallMovVideo() {
        testSplitVideo("earth.mov")
    }

    @Test fun testGetDuration() {
        val duration = VideoSplitter.getVideoDuration(videoSource + "earth.mov")
        assertEquals(30, duration.toInt())
    }

    @Test fun testGetVideoBitrate() {
        val bitrate = VideoSplitter.getVideoBitrate(videoSource + "earth.mov")
        assertEquals(185, bitrate)
    }

    @Test fun testGetAudioBitrate() {
        val bitrate = VideoSplitter.getAudioBitrate(videoSource + "earth.mov")
        assertEquals(139, bitrate)
    }

    private fun testSplitVideo(fileName: String) {
        val video = File(videoSource + fileName)
        val partSize = 100_000 // bytes
        val maxPartCount = ceil((video.length() / partSize).toDouble())

        val createdFiles = VideoSplitter.splitVideo(videoSource + fileName, outputSource, partSize)
        var totalPartSizes = 0L

        for (file in createdFiles) {
            totalPartSizes += File(file).length()
        }

        // Check that the number of parts created does not exceed max expected
        assert(createdFiles.size <= maxPartCount) { "Too many video parts created for $fileName! Found ${createdFiles.size} expected $maxPartCount" }

        // TODO: We could check sum of the parts duration instead
        // Check total of created parts cover the whole video are not too big or too small
        // It is expected for the file size to change based on the encoding, the 50% limit is arbitrary though
        val sizeChangePercentage = 100 * (totalPartSizes - video.length()) / video.length()
        assert(abs(sizeChangePercentage) <= 50) { "Created video parts for $fileName changed by over 50%! Total parts size change: $sizeChangePercentage%" }
    }
}