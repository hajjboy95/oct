package com.octopus.core.enums

// Enum which contains all job types we will implement in the future
enum class JobType {
    VIDEO_TRANSCODE,
    WEB_SCRAPING,
    NONE;

    companion object {
        fun weight(jobType: JobType): Int {
            return when (jobType) {
                VIDEO_TRANSCODE -> 5
                WEB_SCRAPING -> 1
                else -> 0
            }
        }
    }
}