package de.plapadoo.bote

import de.plapadoo.bote.database.DatabaseConnectionFactory
import de.plapadoo.bote.database.api.Database
import de.plapadoo.bote.endpoint.SubscriberEndpoint
import io.reactivex.schedulers.Schedulers
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.logging.LoggingFeature
import org.glassfish.jersey.server.ResourceConfig
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.server.model.Resource
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.LogManager

/**
 * Starts the jetty server to serve the ReST API.

 * @param args Should contain one String with the path/to/the/configuration.properties.
 * *
 * @throws Exception If the jetty server cannot be started.
 */
fun main(args: Array<String>) {
	val logger = LoggerFactory.getLogger("de.plapadoo.bote.Application")
	try {
		if (args.isEmpty()) {
			throw RuntimeException("Please provide the path/to/the/application.properties as the first program argument.")
		}

		val config = ApplicationConfiguration(Paths.get(args[0]))
		val rc = ResourceConfig()
		initializeLogging(rc, config)
		val resourceBuilder = Resource.builder(SubscriberEndpoint::class.java).path( config.path)
		rc.registerResources(resourceBuilder.build())

		DatabaseConnectionFactory.connectToDatabase(config.databaseUrl, config.databaseUsername, config.databasePassword).use { database ->
			rc.register(object : AbstractBinder() {
				override fun configure() {
					this.bind<Database>(database).to(Database::class.java)
					this.bind<ApplicationConfiguration>(config).to(ApplicationConfiguration::class.java)
				}
			})

			val grizzlyServer = GrizzlyHttpServerFactory.createHttpServer(URI("http",
					null,
					"0.0.0.0",
					config.port,
					null,
					null,
					null), rc)

			database.deleteUnconfirmedSubscribers()
					.subscribeOn(Schedulers.io())
					.delay(1, TimeUnit.HOURS)
					.repeat()
					.subscribe{logger.info("Removing unconfirmed subscribers older than 24 hours")}

			grizzlyServer.start()
			while (grizzlyServer.isStarted) Thread.sleep(1000)
		}


	} catch (e: ConfigPropertyNotFoundException) {
		logger.error(e.message)
		System.exit(1)
	} catch (e: Exception) {
		logger.error("error in application", e)
		System.exit(1)
	}
}

private fun initializeLogging(rc: ResourceConfig, config: ApplicationConfiguration) {
	//We prefer to set the logback config programmatically and specify the path in the application configuration.
	System.setProperty("logback.configurationFile", config.logConfigPath.toString())
	LogManager.getLogManager().reset()
	SLF4JBridgeHandler.removeHandlersForRootLogger()
	SLF4JBridgeHandler.install()
	LogManager.getLogManager().getLogger("").level = Level.FINE
	rc.register(LoggingFeature::class.java)
}