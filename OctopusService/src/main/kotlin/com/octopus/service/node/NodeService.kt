package com.octopus.service.node

import com.octopus.core.models.Account
import com.octopus.core.models.Host
import com.octopus.core.models.Node
import com.octopus.core.models.SubJob
import com.octopus.dao.AccountDao
import com.octopus.dao.HostDao
import com.octopus.transport.NodeRegistrationRequest
import com.octopus.transport.ServerMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.websocket.CloseReason
import javax.websocket.Session

object NodeService {
    private val logger: Logger = LoggerFactory.getLogger(NodeService::class.java)
    private var nodes: LinkedHashMap<String, Node> = LinkedHashMap()
    private var currNodeIndex = 0
    private lateinit var hostDao: HostDao
    private lateinit var accountDao: AccountDao

    fun setup(hostDao: HostDao, accountDao: AccountDao) {
        this.hostDao = hostDao
        this.accountDao = accountDao
    }

    fun removeNodeSession(nodeSession: Session) {
        logger.info("Removing node with with sessionId ${nodeSession.id}")
        nodes.remove(nodeSession.id)
    }

    /**
     * Returns the next node to send a [SubJob] to.
     * For now, the strategy is to simply return the next node. We should later move this
     *
     * @return [Node]
     */
    fun getNextNode(): Node? {
        return if (!nodes.isEmpty()) nodes.toList().get(currNodeIndex++ % nodes.size).second else null
    }

    fun hasNodes(): Boolean {
        return !nodes.isEmpty()
    }

    fun getHostBySession(nodeSession: Session): Host? {
        return nodes.get(nodeSession.id)?.host
    }

    fun handleNodeRegistrationRequest(nodeSession: Session, registrationRequest: NodeRegistrationRequest) {
        logger.info("Received registration request from session (${nodeSession.id}) with mac id: ${registrationRequest.macId}")

        try {
            var host = hostDao.findByMacId(registrationRequest.macId!!)
            if (host == null) {
                host = registerAndGetHost(registrationRequest)
            } else {
                // We should confirm the account matches with the registered node
                val account = accountDao.findByUsername(registrationRequest.accountUsername!!) ?: throw Exception("Account not found: ${registrationRequest.accountUsername}")

                if (host.accountId != account.id) {
                    throw Exception("This Node is already registered under a different username. Found account (${registrationRequest.accountUsername})")
                }

                logger.info("Host (${registrationRequest.macId}) is already registered.")
            }

            addNodeSession(Node(host, nodeSession))
        } catch (e: Exception) {
            logger.error(e.message)
            nodeSession.close(CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Failed to register node: ${e.message}"))
        }
    }

    private fun addNodeSession(node: Node) {
        if (isAlreadyAdded(node)) {
            logger.warn("Host (${node.host.macId}) is already connected! Closing connection...")
            node.session.close(CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Attempted to connect on different sockets!"))
        } else {
            nodes.put(node.getSessionId(), node)
            node.send(ServerMessage(ServerMessage.MessageType.REGISTERED, "The node is now registered."))
            logger.info("$node added.")
        }
    }

    /**
     * Registers a node by adding it to the DB.
     */
    @Throws(Exception::class)
    private fun registerAndGetHost(registrationRequest: NodeRegistrationRequest): Host {
        logger.info("Registering host (${registrationRequest.macId}) ...")
        val account: Account = accountDao.findByUsername(registrationRequest.accountUsername!!) ?: throw Exception("Account not found: ${registrationRequest.accountUsername}")
        val hostId = hostDao.addHostForAccount(account.id!!, registrationRequest.macId!!)
        logger.info("Added host to DB.")
        return Host(hostId, registrationRequest.macId)
    }

    // TODO: This is a temp solution. Improve performance later
    // We probably need a map of macId -> Node to be able to check if a Node is already added
    // At a scale where this causes a performance impact though, we probably care more about memory than time for this
    // A single list is probably better than 2 or 3 copies of all the Node objects
    private fun isAlreadyAdded(targetNode: Node) : Boolean {
        val node = nodes.toList().find { node -> node.second.host.macId == targetNode.host.macId }
        return node != null
    }
}