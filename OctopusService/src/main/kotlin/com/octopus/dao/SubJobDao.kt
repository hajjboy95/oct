package com.octopus.dao

import com.octopus.core.enums.PaymentStatus
import com.octopus.core.models.SubJob
import com.octopus.dao.mappers.SubJobMapper
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface SubJobDao {
    @GetGeneratedKeys
    @SqlBatch("INSERT INTO subjob (job_id, uuid, sequence_number) VALUES (:jobId, :uuid, :sequenceNumber)")
    fun addSubJobs(@Bind("jobId") jobId: Int, @BindBean vararg subJob: SubJob): List<Int>

    @RegisterRowMapper(SubJobMapper::class)
    @SqlQuery("SELECT subjob.id as id, subjob.uuid as uuid, job.uuid as job_uuid, sequence_number, job_type, retries FROM subjob " +
            "JOIN job ON (subjob.job_id = job.id) " +
            "WHERE job.uuid = :jobUUID AND subjob.sequence_number = :sequenceNumber")
    fun getSubJobByParentUUIDAndSequenceNumber(@Bind("jobUUID") uuid: String, @Bind("sequenceNumber") sequenceNumber: Int): SubJob

    @SqlUpdate("UPDATE subjob SET retries = retries + 1 where id = :id;")
    fun incrementRetries(@Bind("id") id: Int)

    @RegisterRowMapper(SubJobMapper::class)
    @SqlQuery("SELECT subjob.id as id, subjob.uuid as uuid, subjob.retries as retries, job.uuid as job_uuid, " +
            "sequence_number, job_type FROM subjob INNER JOIN job ON job.id = subjob.job_id " +
            "WHERE subjob.retries < :retryLimit AND subjob.finished_at IS NULL AND " +
            "subjob.updated_at < NOW() - :minutes * '1 MINUTES'::interval")
    fun findInProgressSubJobsOlderThanAndRetryable(@Bind("minutes") minutes: Int, @Bind("retryLimit") retryLimit: Int): List<SubJob>

    // Executions

    @SqlUpdate("UPDATE subjob SET updated_at = NOW() where id = :subjob_id;" +
            "INSERT INTO subjob_execution (subjob_id, host_id) VALUES (:subjob_id, :host_id);")
    fun addSubJobExecutionAndUpdateSubjob(@Bind("subjob_id") subJobId: Int, @Bind("host_id") hostId: Int): Int

    @SqlUpdate("UPDATE subjob SET started_at = NOW(), updated_at = NOW() where id = :subjob_id;" +
            "UPDATE subjob_execution SET started_at = NOW() WHERE subjob_id = :subjob_id AND host_id = :host_id;")
    fun updateStartedAt(@Bind("subjob_id") subJobId: Int, @Bind("host_id") hostId: Int)

    @SqlUpdate("UPDATE subjob SET updated_at = NOW(), finished_at = NOW() where id = :subjob_id;" +
            "UPDATE subjob_execution SET finished_at = NOW() WHERE subjob_id = :subjob_id AND host_id = :host_id")
    fun updateExecutionAndSubJobFinishedAt(@Bind("subjob_id") subJobId: Int, @Bind("host_id") hostId: Int)

    @SqlUpdate("UPDATE subjob SET payment_status = :paymentStatus::PAYMENT_STATUS where id = :id;")
    fun updateSubJobPaymentStatus(@Bind("id") id: Int, @Bind("paymentStatus") paymentStatus: PaymentStatus)
}