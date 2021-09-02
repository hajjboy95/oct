package com.octopus.utils

import com.fasterxml.jackson.databind.ObjectMapper
import java.sql.ResultSet

fun containsColumn(rs: ResultSet, columnName: String) : Boolean {
    val metaData = rs.metaData
    val columns = metaData.columnCount

    for (column in 1..columns) {
        if (columnName == metaData.getColumnName(column)) {
            return true
        }
    }
    return false
}

fun prettyPrint(any: Any) : String {
    val mapper = ObjectMapper()
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(any)
}
