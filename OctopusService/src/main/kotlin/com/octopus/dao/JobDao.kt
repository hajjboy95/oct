package com.octopus.dao

import com.octopus.core.models.Job
import com.octopus.core.models.UserJob
import com.octopus.dao.mappers.UserJobMapper
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@RegisterRowMapper(UserJobMapper::class)
interface JobDao {
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO job (job_type, uuid, account_id) VALUES (:jobType::JOBTYPE, :uuid, :accountId);")
    fun addJob(@BindBean job: Job): Int

    @SqlQuery("SELECT * FROM job WHERE uuid = :uuid")
    fun getByUUID(@Bind("uuid") uuid: String) : UserJob?

    @SqlQuery("SELECT uuid FROM job WHERE id = :id;")
    fun getUUIDFor(@Bind("id") id: Int) : String?

    @SqlQuery("SELECT id, uuid, account_id, job_type, created_at, updated_at, started_at, finished_at FROM job WHERE account_id = :accountId")
    fun getAllForAccount(@Bind("accountId") accountId: Int) : List<UserJob>

    @SqlQuery("SELECT * FROM job WHERE uuid = :uuid and account_id = :accountId")
    fun getByUUID(@Bind("uuid") uuid: String, @Bind("accountId") accountId: Int) : UserJob?

    @SqlQuery("SELECT job_id FROM subjob WHERE id = :subJobId")
    fun getJobIdBySubJobId(@Bind("subJobId") subJobId: Int): Int

    @SqlQuery("SELECT * FROM job")
    fun getAll() : List<UserJob>

    @SqlUpdate("UPDATE job " +
            "SET started_at = NOW(), updated_at = NOW() " +
            "WHERE id = :jobId AND started_at IS NULL")
    fun updateJobStartedAtIfNotUpdated(@Bind("jobId") subJobId: Int)

    @SqlUpdate("UPDATE job SET started_at = NOW() where id = :id;")
    fun updateStartedAt(@Bind("id") id: Int)

    @SqlUpdate("UPDATE job SET finished_at = NOW() where id = :id;")
    fun updateFinishedAt(@Bind("id") id: Int)

    @SqlUpdate("UPDATE job SET finished_at = NOW() where uuid = :uuid;")
    fun updateFinishedAt(@Bind("uuid") uuid: String)
}