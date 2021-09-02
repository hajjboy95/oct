package com.octopus.core.models.payloads

import java.io.Serializable

data class VideoPayload(val videoLink: String, val sourceEncoding: String, val targetEncoding: String) : Serializable
