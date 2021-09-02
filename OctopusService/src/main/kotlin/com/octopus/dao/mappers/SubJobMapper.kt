package com.octopus.dao.mappers

import com.octopus.core.enums.JobType
import com.octopus.core.models.SubJob
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class SubJobMapper: RowMapper<SubJob> {
    override fun map(rs: ResultSet, ctx: StatementContext): SubJob {
        return SubJob(
                rs.getInt("id"),
                rs.getString("uuid"),
                rs.getString("job_uuid"),
                Any(),
                rs.getInt("sequence_number"),
                JobType.valueOf(rs.getObject("job_type") as String),
                rs.getInt("retries")
        )
    }
}
