package com.octopus.core.auth

import com.octopus.core.models.Account
import java.security.Principal

class User(val account: Account) : Principal {
    override fun getName(): String {
        return account.username!!
    }

    fun getId(): Int {
        return account.id!!
    }
}