package com.octopus.dao.mappers

import com.octopus.core.models.Account
import com.octopus.utils.containsColumn
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class AccountMapper: RowMapper<Account> {
    override fun map(rs: ResultSet, ctx: StatementContext): Account {
        return Account(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("email"),
                if (containsColumn(rs, "password")) rs.getString("password") else null
        )
    }
}
