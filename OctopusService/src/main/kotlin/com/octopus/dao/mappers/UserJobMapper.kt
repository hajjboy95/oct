package com.octopus.dao.mappers

import com.octopus.core.enums.JobType
import com.octopus.core.models.Account
import com.octopus.core.models.UserJob
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class UserJobMapper: RowMapper<UserJob> {
    override fun map(rs: ResultSet, ctx: StatementContext): UserJob {
        return UserJob(
                rs.getInt("id"),
                rs.getString("uuid"),
                rs.getInt("account_id"),
                JobType.valueOf(rs.getString("job_type")),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at"),
                rs.getTimestamp("started_at"),
                rs.getTimestamp("finished_at")
        )
    }
}
