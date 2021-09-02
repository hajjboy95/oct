package com.octopus.configurations

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import io.dropwizard.db.DataSourceFactory
import org.knowm.dropwizard.sundial.SundialConfiguration
import javax.validation.Valid
import javax.validation.constraints.NotNull

class OctopusConfiguration : Configuration() {
    @Valid
    @NotNull
    var database = DataSourceFactory()

    @Valid
    val name: String? = null

    @Valid
    val realm: String? = null

    @Valid
    val jwtSecret: String? = null

    @JsonProperty("sundial")
    @Valid
    @NotNull
    var sundialConfiguration  = SundialConfiguration()
}
