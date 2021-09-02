package com.octopus.dao

import com.octopus.dao.mappers.AccountMapper
import com.octopus.core.models.Account
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@RegisterRowMapper(AccountMapper::class)
interface AccountDao {
    @SqlQuery("SELECT id, username, email FROM account")
    fun findAll() : List<Account>

    @SqlQuery("SELECT * FROM account WHERE id = :accountId")
    fun findById(@Bind("accountId") accountId: Int): Account?

    @SqlQuery("SELECT * FROM account WHERE username = :username")
    fun findByUsername(@Bind("username") username: String): Account?

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO account (username, email, password) VALUES (:username, :email, :password)")
    fun addAccount(@BindBean account: Account) : Int
}
