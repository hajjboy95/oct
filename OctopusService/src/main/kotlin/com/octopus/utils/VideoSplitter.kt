package com.octopus.utils

import org.slf4j.LoggerFactory
import ws.schild.jave.DefaultFFMPEGLocator
import ws.schild.jave.FFMPEGExecutor
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * This class splits a video into multiple parts.
 * This implementation is not great and is a bit flaky especially with larger files.
 */
object VideoSplitter {
    private val logger = LoggerFactory.getLogger(VideoSplitter::class.java)

    @Throws(IOException::class, InterruptedException::class)
    fun splitVideo(sourcePath: String, targetPath: String, partSizeInBytes: Int) : List<String> {
        val filePaths = mutableListOf<String>()
        val ffmpegExecutablePath: String = DefaultFFMPEGLocator().ffmpegExecutablePath
        val fileExt = getFileExtension(sourcePath)
        val totalDuration = getVideoDuration(sourcePath)
        val videoBitrate = getVideoBitrate(sourcePath)
        val audioBitrate = getAudioBitrate(sourcePath)
        var offset = 0.0
        var count = 1

        // While offset is still not the end of the video
        // TODO: Use doubles to avoid video loss
        //  We cast to int to avoid problems with double comparision
        //  Max video length that could be lost is 1 second
        while (offset.toInt() < totalDuration.toInt()) {
            val ffmpegExecutor = FFMPEGExecutor(ffmpegExecutablePath)
            val partFileName = "$count.$fileExt"
            val targetFilePath = targetPath + partFileName

            logger.info("Creating video section for $offset / $totalDuration in $targetFilePath")

            ffmpegExecutor.addArgument("-ss")
            ffmpegExecutor.addArgument(offset.toString())
            ffmpegExecutor.addArgument("-i")
            ffmpegExecutor.addArgument(sourcePath)
            ffmpegExecutor.addArgument("-fs")
            ffmpegExecutor.addArgument(partSizeInBytes.toString())
            ffmpegExecutor.addArgument("-b:v")
            ffmpegExecutor.addArgument("${videoBitrate}K")
            ffmpegExecutor.addArgument("-b:a")
            ffmpegExecutor.addArgument("${audioBitrate}K")
            ffmpegExecutor.addArgument(targetFilePath)
            ffmpegExecutor.addArgument("-y")
            ffmpegExecutor.execute()

            // Doesn't work too well. Hangs for some reason. Never completes even after the file is created.
            // ffmpegExecutor.processExitCode

            offset += attemptToGetVideoDuration(targetFilePath, 50, 2000)
            count++

            filePaths.add(targetFilePath)
        }

        return filePaths
    }

    @Throws(IOException::class)
    fun getVideoDuration(filePath: String?): Double {
        val ffmpegExecutor = FFMPEGExecutor(DefaultFFMPEGLocator().ffmpegExecutablePath)

        ffmpegExecutor.addArgument("-i")
        ffmpegExecutor.addArgument(filePath)
        ffmpegExecutor.execute()

        val errorStream: InputStream = ffmpegExecutor.errorStream
        val streamReader = InputStreamReader(errorStream)
        val reader = BufferedReader(streamReader)
        var duration: String? = null

        // Get duration string from file output. Syntax: HH:MM:SS.MS
        reader.forEachLine {
            var line = it
            if (line.contains("Duration:")) {
                line = line.replaceFirst("Duration: ".toRegex(), "").trim { it <= ' ' }
                duration = line.substring(0, 11)
                return@forEachLine
            }
        }

        if (duration == null) {
            return (-1).toDouble()
        }

        // Parse duration
        val timeSections = duration!!.split(":").toTypedArray()
        val hours = timeSections[0].toInt()
        val minutes = timeSections[1].toInt()
        val seconds = timeSections[2].toDouble()

        return seconds + minutes * 60 + hours * 3600
    }

    private fun attemptToGetVideoDuration(filePath: String, retryLimit: Int, waitTimeInMs: Long): Double {
        var duration = -1.0
        var retryCount = 0

        while (duration < 0 && retryCount++ < retryLimit) {
            try {
                logger.info("Attempt #" + retryCount + " for " + getFileName(filePath))
                logger.info("Sleeping " + waitTimeInMs / 1000 + " seconds...")
                Thread.sleep(waitTimeInMs)
                duration = getVideoDuration(filePath)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        if (duration < 0) {
            throw RuntimeException("Could not get duration for " + filePath + " after waiting " + retryLimit * waitTimeInMs + "ms.")
        }

        return duration
    }

    @Throws(IOException::class)
    fun getVideoBitrate(filePath: String?): Int {
        val ffmpegExecutor = FFMPEGExecutor(DefaultFFMPEGLocator().ffmpegExecutablePath)

        ffmpegExecutor.addArgument("-i")
        ffmpegExecutor.addArgument(filePath)
        ffmpegExecutor.execute()

        val errorStream: InputStream = ffmpegExecutor.errorStream
        val streamReader = InputStreamReader(errorStream)
        val reader = BufferedReader(streamReader)
        var bitrate: String? = null

        // Get bitrate string from file output. Syntax: ... bitrate: INT kb/s
        reader.forEachLine {
            var line = it
            if (line.contains("bitrate:")) {
                line = line.substring(line.indexOf("bitrate"))
                bitrate = line.substring(9, line.length - 5)
                return@forEachLine
            }
        }

        if (bitrate == null) {
            return -1
        }

        return bitrate!!.toInt()
    }

    @Throws(IOException::class)
    fun getAudioBitrate(filePath: String?): Int {
        val ffmpegExecutor = FFMPEGExecutor(DefaultFFMPEGLocator().ffmpegExecutablePath)

        ffmpegExecutor.addArgument("-i")
        ffmpegExecutor.addArgument(filePath)
        ffmpegExecutor.execute()

        val errorStream: InputStream = ffmpegExecutor.errorStream
        val streamReader = InputStreamReader(errorStream)
        val reader = BufferedReader(streamReader)
        var bitrate: String? = null

        // Get audio bitrate string from file output. Syntax:
        // Stream #0:1(eng): Audio: aac (LC) (mp4a / 0x6134706D), 48000 Hz, stereo, fltp, 139 kb/s (default)
        reader.forEachLine {
            var line = it
            if (line.contains("Audio:")) {
                val endIndex = line.indexOf(" kb/s")
                // Set startIndex to last digit of bitrate
                var startIndex = endIndex - 1

                // Decrement startIndex until the next space
                while (line[startIndex - 1] != ' ') {
                    startIndex--
                }

                bitrate = line.substring(startIndex, endIndex)
                return@forEachLine
            }
        }

        if (bitrate == null) {
            return -1
        }

        return bitrate!!.toInt()
    }
}