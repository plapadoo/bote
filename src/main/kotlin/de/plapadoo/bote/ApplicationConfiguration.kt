package de.plapadoo.bote

import java.io.IOException
import java.nio.file.*
import java.util.*

class ConfigPropertyNotFoundException(message: String) : RuntimeException(message)

class ApplicationConfiguration
/**
 * Constructor to load a properties file from the given path.

 * @param path The path to the properties file.
 * *
 * @throws IOException If the properties file cannot be read.
 */
constructor(path: Path) {
	private val properties = Properties().apply {
		try {
			Files.newBufferedReader(path).use { r -> this.load(r) }
		} catch (ignored: NoSuchFileException) {
			throw RuntimeException("couldn't find configuration file “$path”")
		}
	}

	/**
	 * @return The port for the server to serve the ReST API on.
	 */
	val port: Int
		get() = this.checkProperty(KEY_PORT).let {
			it.toIntOrNull() ?: throw RuntimeException("The port “$it” is not valid")
		}

	val logConfigPath: Path
		get() {
			val property = this.checkProperty(KEY_LOG_CONFIG)
			try {
				val path = Paths.get(property)
				return if (!Files.isReadable(path)) {
					throw RuntimeException("The log config at path “$property” does not exist or is not readable.")
				} else path
			} catch (ignored: InvalidPathException) {
				throw RuntimeException("The log config path “$property” is not valid")
			}

		}

	private fun checkProperty(propertyKey: String) = this.properties.getProperty(propertyKey) ?: throw ConfigPropertyNotFoundException(
			"Configuration is missing property: “$propertyKey”")

	val databaseUrl: String
		get() = this.checkProperty(KEY_DB_URL)

	val databaseUsername: String?
		get() = this.properties.getProperty(KEY_DB_USERNAME)

	val databasePassword: String?
		get() = this.properties.getProperty(KEY_DB_PASSWORD)

	val confirmSuccessUrl: String
		get() = this.properties.getProperty(KEY_CONFIRM_SUCCESS_URL)

	val confirmFailureUrl: String
		get() = this.properties.getProperty(KEY_CONFIRM_FAILURE_URL)

	val unsubscribeSuccessUrl: String
		get() = this.properties.getProperty(KEY_UNSUBSCRIBE_SUCCESS_URL)

	val unsubscribeFailureUrl: String
		get() = this.properties.getProperty(KEY_UNSUBSCRIBE_FAILURE_URL)

	val subscribeSuccessUrl: String
		get() = this.properties.getProperty(KEY_SUBSCRIBE_SUCCESS_URL)

	val subscribeFailureUrl: String
		get() = this.properties.getProperty(KEY_SUBSCRIBE_FAILURE_URL)

	val subscribeAlreadySubscribedUrl: String
		get() = this.properties.getProperty(KEY_SUBSCRIBE_ALREADY_SUBSCRIBED_URL)

	companion object {
		const val KEY_PORT = "port"
		const val KEY_LOG_CONFIG = "log.config"
		const val KEY_DB_URL = "db.url"
		const val KEY_DB_USERNAME = "db.username"
		const val KEY_DB_PASSWORD = "db.password"
		const val KEY_CONFIRM_SUCCESS_URL = "confirm.success.url"
		const val KEY_CONFIRM_FAILURE_URL = "confirm.failure.url"
		const val KEY_UNSUBSCRIBE_FAILURE_URL = "unsubscribe.failure.url"
		const val KEY_UNSUBSCRIBE_SUCCESS_URL = "unsubscribe.success.url"
		const val KEY_SUBSCRIBE_FAILURE_URL = "subscribe.failure.url"
		const val KEY_SUBSCRIBE_SUCCESS_URL = "subscribe.success.url"
		const val KEY_SUBSCRIBE_ALREADY_SUBSCRIBED_URL = "subscribe.already.subscribed.url"
	}
}
