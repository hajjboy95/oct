package com.octopus.core.models

import javax.websocket.Session

/**
 * Wrapper class around a web socket [Session].
 * Used to extend functionality and store node metadata.
 */
class Node(val host: Host, val session: Session) {
    private var sentSubJobCount = 0
    private var completedSubJobCount = 0

    fun send(obj : Any) {
        session.asyncRemote.sendObject(obj)
        sentSubJobCount++
    }

    fun incrementSuccess() {
        completedSubJobCount++
    }

    fun getSessionId() : String {
        return session.id
    }

    override fun toString() : String {
        return "Node#${host.id} (${host.macId})"
    }
}