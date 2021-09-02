package com.octopus

import com.octopus.configurations.OctopusConfiguration
import com.octopus.core.auth.BasicAuthenticator
import com.octopus.core.auth.TokenAuthenticator
import com.octopus.core.auth.User
import com.octopus.dao.AccountDao
import com.octopus.dao.HostDao
import com.octopus.dao.JobDao
import com.octopus.dao.SubJobDao
import com.octopus.resources.*
import com.octopus.resources.websockets.NodeWebSocketEndpoint
import com.octopus.service.AllJobExecutorService
import com.octopus.service.JobExecutors
import com.octopus.service.node.NodeService
import io.dropwizard.Application
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthFilter
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.auth.basic.BasicCredentialAuthFilter
import io.dropwizard.auth.chained.ChainedAuthFilter
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter
import io.dropwizard.forms.MultiPartBundle
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.websockets.WebsocketBundle
import org.eclipse.jetty.servlets.CrossOriginFilter
import org.flywaydb.core.Flyway
import org.knowm.dropwizard.sundial.SundialBundle
import org.knowm.dropwizard.sundial.SundialConfiguration
import org.slf4j.LoggerFactory
import java.util.*
import javax.servlet.DispatcherType
import javax.websocket.server.ServerEndpointConfig


class OctopusApp : Application<OctopusConfiguration>() {
    private val logger = LoggerFactory.getLogger(OctopusApp::class.java)
    private val DB_NAME = "postgresql"
    private var webSocketConfig: ServerEndpointConfig? = null

    override fun initialize(bootstrap: Bootstrap<OctopusConfiguration>) {
        webSocketConfig = ServerEndpointConfig.Builder.create(NodeWebSocketEndpoint::class.java, "/ws").build()
        bootstrap.addBundle(WebsocketBundle(webSocketConfig))
        bootstrap.addBundle(object : SundialBundle<OctopusConfiguration>() {
            override fun getSundialConfiguration(configuration: OctopusConfiguration): SundialConfiguration {
                return configuration.sundialConfiguration
            }
        })
        bootstrap.addBundle(MultiPartBundle())
    }

    override fun run(config: OctopusConfiguration, environment: Environment) {
        logger.info("Running ${config.name!!} in realm: ${config.realm!!}")

        val flyway: Flyway = Flyway.configure().dataSource(config.database.url, config.database.user, config.database.password).load()
        flyway.migrate()

        val jdbi = JdbiFactory().build(environment, config.database, DB_NAME)

        val hostDao = jdbi.onDemand(HostDao::class.java)
        val accountDao = jdbi.onDemand(AccountDao::class.java)
        val jobDao = jdbi.onDemand(JobDao::class.java)
        val subJobDao = jdbi.onDemand(SubJobDao::class.java)

        val jobExecutors = JobExecutors(jobDao, subJobDao)
        val jobExecutorService = AllJobExecutorService(jobExecutors, subJobDao, jobDao)

        NodeService.setup(hostDao, accountDao)
        setupAuth(config, environment, accountDao)
        webSocketConfig!!.userProperties[NodeWebSocketEndpoint.JOB_SERVICE_KEY] = jobExecutorService

        environment.jersey().register(HostResource(hostDao))
        environment.jersey().register(AccountResource(accountDao))
        environment.jersey().register(JobResource(jobDao, jobExecutorService))
        environment.jersey().register(HealthResource())
        environment.jersey().register(FileResource())
        environment.jersey().register(ResultResource(jobDao, jobExecutors))

        // minimise these to Cron jobs only
        environment.applicationContext.setAttribute(subjobDaoKey, subJobDao) // to access it from schedulers
        environment.applicationContext.setAttribute(jobExecutorsKey, jobExecutors) // to access it from schedulers
        environment.applicationContext.setAttribute(hostDaoKey, hostDao) // to access it from schedulers
        configureCors(environment)
    }

    private fun setupAuth(config: OctopusConfiguration, env: Environment, accountDao: AccountDao) {
        val jwtSecret: ByteArray = config.jwtSecret!!.toByteArray()
        val basicAuthenticator = BasicAuthenticator(accountDao)
        val tokenAuthenticator = TokenAuthenticator(accountDao, jwtSecret)

        val basicCredentialAuthFilter: AuthFilter<*, *> = BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(basicAuthenticator)
                .setPrefix("Basic")
                .buildAuthFilter()

        val oauthCredentialAuthFilter: AuthFilter<*, *> = OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(tokenAuthenticator)
                .setPrefix("Bearer")
                .buildAuthFilter()

        val handlers = listOf(basicCredentialAuthFilter, oauthCredentialAuthFilter)

        env.jersey().register(AuthDynamicFeature(ChainedAuthFilter<Any, User>(handlers)))
        env.jersey().register(AuthValueFactoryProvider.Binder(User::class.java))

        env.jersey().register(AuthResource(tokenAuthenticator))
    }

    private fun configureCors(environment: Environment) {
        val cors = environment.servlets().addFilter("CORS", CrossOriginFilter::class.java)

        // Configure CORS parameters
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*")
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization")
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD")
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true")

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")
    }
}
