package com.octopus.utils

import com.google.common.annotations.VisibleForTesting
import org.slf4j.LoggerFactory
import ws.schild.jave.DefaultFFMPEGLocator
import ws.schild.jave.FFMPEGExecutor
import java.io.*
import java.lang.RuntimeException
import java.lang.Thread.sleep
import kotlin.math.log
import kotlin.streams.asStream
import kotlin.streams.toList

/**
 * This class combines multiple videos into 1 video file.
 */
object VideoConcatenator {
    private val log = LoggerFactory.getLogger(VideoConcatenator::class.java)

    fun concatVideosInDir(sourceDir: String, outputPath: String) : Int {
        log.info("Concatenating videos in dir: $sourceDir")
        return concatVideos(getAllChildPaths(sourceDir), outputPath)
    }

    @VisibleForTesting
    fun getAllChildPaths(sourceDir: String) : List<String> {
        val dir = File(sourceDir)
        return dir.walk().maxDepth(1).asStream()
                .map { file -> file.path.replace("\\", "/") }
                .filter { path -> path != sourceDir.replace("\\", "/") }
                .toList()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun concatVideos(sourceVideos: List<String>, outputPath: String) : Int {
        log.info("Getting the FFMPEG Executor...")
        val ffmpegExecutablePath: String = DefaultFFMPEGLocator().ffmpegExecutablePath
        val ffmpegExecutor = FFMPEGExecutor(ffmpegExecutablePath)

        // Create parent dir if not exists or fail if cannot create it
        val parentDir = File(outputPath).parentFile
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw RuntimeException("Cannot create parent directory: ${parentDir.path}")
        }

        // Create tmp file with file paths
        log.info("Creating the tmp file with the list of paths...")
        val tmpFile = createTempFile()
        sourceVideos.forEach { tmpFile.appendText("file $it" + System.lineSeparator()) }

        // This method avoids re-encoding the video files
        log.info("Starting concatenation...")
        ffmpegExecutor.addArgument("-f")
        ffmpegExecutor.addArgument("concat")
        ffmpegExecutor.addArgument("-safe")
        ffmpegExecutor.addArgument("0")
        ffmpegExecutor.addArgument("-i")
        ffmpegExecutor.addArgument(tmpFile.path)
        ffmpegExecutor.addArgument("-c")
        ffmpegExecutor.addArgument("copy")
        ffmpegExecutor.addArgument(outputPath)
        ffmpegExecutor.execute()
        log.info("Command started. Output: $outputPath")

        // Wait until the file is created
        log.info("Waiting for concatenation to complete...")
        val retVal = ffmpegExecutor.processExitCode
        log.info("Concatenation completed! Exit code: $retVal")
        return retVal
    }
}