package com.octopus.core.models.payloads

import java.io.Serializable

data class WebScrapingPayload(val urls: List<String>, val scrapingTags: List<String>): Serializable