package de.plapadoo.bote.endpoint

import de.plapadoo.bote.ApplicationConfiguration
import de.plapadoo.bote.database.api.Database
import de.plapadoo.bote.util.MailUtil
import org.slf4j.LoggerFactory
import java.net.URI
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class SubscriberEndpoint @Inject constructor(val database: Database, private val config: ApplicationConfiguration) {

	val LOG = LoggerFactory.getLogger(SubscriberEndpoint::class.java)!!

	@GET
	fun get(@QueryParam("confirm") token: String?, @QueryParam("unsubscribe") deleteEmail: String?): Response {
		when {
			token != null -> {
				LOG.info("Tryring to activate subscription with token: $token")
				return database.confirmSubscriber(token)
						.map { Response.temporaryRedirect(URI.create(if (it) config.confirmSuccessUrl else config.confirmFailureUrl)).build() }
						.blockingGet()

			}
			deleteEmail != null -> {
				LOG.info("Trying to remove subscription for email: $deleteEmail")
				return database.deleteSubscriber(deleteEmail)
						.toSingle { Response.temporaryRedirect(URI.create(config.unsubscribeSuccessUrl)).build() }
						.onErrorReturn { Response.temporaryRedirect(URI.create(config.unsubscribeFailureUrl)).build() }
						.blockingGet()

			}
			else -> return Response.status(Response.Status.BAD_REQUEST).build()
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	fun post(@FormParam("email") email: String): Response {
		return database.createSubscriber(email).map {
			MailUtil.validate(email)
			when {
				it.isNotBlank() -> {
					LOG.info("New subscriber: $email token: $it")
					MailUtil.sendConfirmationLink(email, it, config)
					Response.temporaryRedirect(URI.create(config.subscribeSuccessUrl)).build()
				}
				else -> {
					LOG.info("Subscriber already in database: $email")
					Response.temporaryRedirect(URI.create(config.subscribeAlreadySubscribedUrl)).build()
				}
			}
		}
		.onErrorReturn { Response.temporaryRedirect(URI.create(config.subscribeFailureUrl)).build() }
		.blockingGet()
	}

}
