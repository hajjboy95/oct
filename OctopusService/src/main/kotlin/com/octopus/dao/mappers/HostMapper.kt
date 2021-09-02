package com.octopus.dao.mappers

import com.octopus.core.models.Host
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class HostMapper: RowMapper<Host> {
    override fun map(rs: ResultSet, ctx: StatementContext): Host {
        return Host(rs.getInt("id"), rs.getString("mac_id"), rs.getInt("account_id"))
    }
}
