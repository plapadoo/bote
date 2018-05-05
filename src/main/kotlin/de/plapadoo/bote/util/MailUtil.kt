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
package de.plapadoo.bote.util

import de.plapadoo.bote.ApplicationConfiguration
import org.slf4j.LoggerFactory
import java.util.*
import java.util.regex.Pattern
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class MailUtil {
	companion object {
		private const val URL_VARIABLE = "\$confirmationUrl"
		private val LOG = LoggerFactory.getLogger(MailUtil::class.java)!!

		fun validate(email: String) {
			InternetAddress(email).validate()
		}

		fun sendConfirmationLink(subscriber: String, token: String, config: ApplicationConfiguration) {
			val props = Properties()
			props.put("mail.smtp.host", config.mailSmtpHost)
			props.put("mail.smtp.port", config.mailSmtpPort)
			val session = Session.getInstance(props, null)

			try {
				val msg = MimeMessage(session)
				msg.setFrom(InternetAddress(config.mailConfirmationFrom))
				msg.setRecipients(Message.RecipientType.TO, subscriber)
				msg.setSubject(config.mailConfirmationSubject)
				msg.setSentDate(Date())
				val confirmationUrl = config.publicUrl + "?confirm=$token"
				msg.setText(
						config.confirmationMailTemplate.replace(URL_VARIABLE, confirmationUrl),
						"utf-8",
						config.mailConfirmationMimeSubtype
				)
				Transport.send(msg)
				LOG.info("Sent confirmation mail with token $token to subscriber $subscriber")
			} catch (mex: MessagingException) {
				LOG.error("Failed to send confirmation mail: ", mex)
			}
		}
	}
}