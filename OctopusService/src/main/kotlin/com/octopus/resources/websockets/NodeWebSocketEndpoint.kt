package com.octopus.resources.websockets

import com.octopus.service.AllJobExecutorService
import com.octopus.service.node.NodeService
import com.octopus.service.video.VideoMakerService
import com.octopus.transport.*
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint(
        value = "/ws",
        encoders = [Encoders.SubJobEncoder::class, Encoders.ServerMessageEncoder::class],
        decoders = [UniversalDecoder::class])
class NodeWebSocketEndpoint {
    private val logger = LoggerFactory.getLogger(NodeWebSocketEndpoint::class.java)

    private lateinit var jobExecutorService: AllJobExecutorService

    companion object {
        const val JOB_SERVICE_KEY = "JOB_SERVICE_KEY"
    }

    @OnOpen
    @Throws(IOException::class)
    fun onOpen(session: Session, config: EndpointConfig) {
        logger.info("Session (${session.id}) connected.")
        jobExecutorService = config.userProperties[JOB_SERVICE_KEY] as AllJobExecutorService
        session.asyncRemote.sendObject(ServerMessage(ServerMessage.MessageType.NOT_REGISTERED, "The node is not registered."))
    }

    /** TODO: Keep this limit as the default (65_536 ~ 64kB)
     *   We currently transfer full video files through the websockets,
     *   but eventually we should upload the video to S3 / FTP server
     *   and only send the link to the video.
     *   We current send videos in parts of 200_000 bytes per SubJob (see [VideoMakerService.PART_SIZE_BYTES]).
     *   Depending on the encoding, the result video could be slightly larger or smaller than this.
     *   For this reason, we set the [maxMessageSize] to 3 * [VideoMakerService.PART_SIZE_BYTES]
     */
    @OnMessage(maxMessageSize = 600_000)
    fun onMessage(session: Session, message: Any) {
        logger.info("Received message of type ${message.javaClass}: $message")
        when (message.javaClass) {
            SubJobResult::class.java -> handle(session, message as SubJobResult)
            NodeRegistrationRequest::class.java -> handle(session, message as NodeRegistrationRequest)
            SubJobUpdate::class.java -> handle(session, message as SubJobUpdate)
        }
    }

    private fun handle(session: Session, subJobStarted: SubJobUpdate) {
        jobExecutorService.handleSubJobStartedAt(session, subJobStarted)
    }

    private fun handle(session: Session, registrationRequest: NodeRegistrationRequest) {
        NodeService.handleNodeRegistrationRequest(session, registrationRequest)
    }

    private fun handle(session: Session, subJobResult: SubJobResult) {
        jobExecutorService.handleSubJobResult(subJobResult, session)
    }

    @OnClose
    fun onClose(session: Session?, closeReason: CloseReason?) {
        NodeService.removeNodeSession(session!!)
        logger.info("Session (${session.id}) closed.")
    }

    @OnError
    fun onError(session: Session?, throwable: Throwable) {
        logger.info("An error occurred with one of the sockets. $throwable")
    }
}
