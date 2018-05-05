/*
 * Copyright 2018 plapadoo UG (haftungsbeschränkt)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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

@Path("/")
class SubscriberEndpoint @Inject constructor(val database: Database, private val config: ApplicationConfiguration) {

	private val LOG = LoggerFactory.getLogger(SubscriberEndpoint::class.java)!!

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
				LOG.info("Trying to remove subscription for email: ${deleteEmail.toLowerCase()}")
				return database.deleteSubscriber(deleteEmail.toLowerCase())
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
		MailUtil.validate(email.toLowerCase())
		return database.createSubscriber(email.toLowerCase()).map {
			when {
				it.isNotBlank() -> {
					LOG.info("New subscriber: ${email.toLowerCase()} token: $it")
					MailUtil.sendConfirmationLink(email, it, config)
					Response.temporaryRedirect(URI.create(config.subscribeSuccessUrl)).build()
				}
				else -> {
					LOG.info("Subscriber already in database: ${email.toLowerCase()}")
					Response.temporaryRedirect(URI.create(config.subscribeAlreadySubscribedUrl)).build()
				}
			}
		}
		.onErrorReturn { Response.temporaryRedirect(URI.create(config.subscribeFailureUrl)).build() }
		.blockingGet()
	}

}
