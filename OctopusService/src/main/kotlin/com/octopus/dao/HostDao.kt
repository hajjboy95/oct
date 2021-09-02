package com.octopus.dao

import com.octopus.core.enums.PaymentStatus
import com.octopus.dao.mappers.HostMapper
import com.octopus.core.models.Host
import com.octopus.core.models.HostSubJobsExecuted
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@RegisterRowMapper(HostMapper::class)
interface HostDao {
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO host (account_id, mac_id) VALUES (:accountId, :macId)")
    fun addHostForAccount(@Bind("accountId") accountId: Int, @Bind("macId") macId: String) : Int

    @SqlQuery("SELECT * FROM host WHERE account_id = :accountId")
    fun findAllForAccount(@Bind("accountId") accountId: Int) : List<Host>

    @SqlQuery("SELECT * FROM host WHERE mac_id = :mac_id")
    fun findByMacId(@Bind("mac_id") macId: String): Host?

    @SqlUpdate("DELETE FROM host WHERE mac_id = :mac_id")
    fun remove(@Bind("mac_id") macId: String)

    @SqlQuery("SELECT host.id AS hostId, mac_id AS macId, subjob.id AS subJobId, job_type, job.id AS jobId, job.account_id AS accountId, payment_status\n" +
            "FROM host\n" +
            "         JOIN subjob_execution ON host_id = host.id\n" +
            "         JOIN subjob ON subjob_execution.subjob_id = subjob.id\n" +
            "         JOIN job ON subjob.job_id = job.id\n" +
            "WHERE subjob.payment_status = :paymentStatus::PAYMENT_STATUS AND subjob.finished_at IS NOT NULL\n" +
            "GROUP BY subjob.id, mac_id, host.id, job_type, job.id, job.account_id;")
    @RegisterRowMapper(HostsSubJobExecutedMapper::class)
    fun getHostSubJobExecutedWithPaymentStatus(@Bind("paymentStatus") paymentStatus: PaymentStatus): List<HostSubJobsExecuted>
}
