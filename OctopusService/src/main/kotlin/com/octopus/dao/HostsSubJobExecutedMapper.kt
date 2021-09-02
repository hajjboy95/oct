package com.octopus.dao

import com.octopus.core.enums.JobType
import com.octopus.core.models.HostSubJobsExecuted
import com.octopus.core.enums.PaymentStatus
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class HostsSubJobExecutedMapper : RowMapper<HostSubJobsExecuted> {
    override fun map(rs: ResultSet, ctx: StatementContext?): HostSubJobsExecuted {
        return HostSubJobsExecuted(
                rs.getInt("hostid"),
                rs.getString("macid"),
                rs.getInt("subjobid"),
                JobType.valueOf(rs.getString("job_type")),
                rs.getInt("jobid"),
                rs.getInt("accountid"),
                PaymentStatus.valueOf(rs.getString("payment_status"))
        )
    }
}