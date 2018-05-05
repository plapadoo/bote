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
package de.plapadoo.bote

import java.nio.charset.Charset
import java.nio.file.*
import java.util.*

class ConfigPropertyNotFoundException(message: String) : RuntimeException(message)

class ApplicationConfiguration(path: Path) {

	private var properties: Properties
	private var confirmationMailText: String

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
		get() = this.checkProperty(KEY_CONFIRM_SUCCESS_URL)

	val confirmFailureUrl: String
		get() = this.checkProperty(KEY_CONFIRM_FAILURE_URL)

	val unsubscribeSuccessUrl: String
		get() = this.checkProperty(KEY_UNSUBSCRIBE_SUCCESS_URL)

	val unsubscribeFailureUrl: String
		get() = this.checkProperty(KEY_UNSUBSCRIBE_FAILURE_URL)

	val subscribeSuccessUrl: String
		get() = this.checkProperty(KEY_SUBSCRIBE_SUCCESS_URL)

	val subscribeFailureUrl: String
		get() = this.checkProperty(KEY_SUBSCRIBE_FAILURE_URL)

	val subscribeAlreadySubscribedUrl: String
		get() = this.checkProperty(KEY_SUBSCRIBE_ALREADY_SUBSCRIBED_URL)

	val mailSmtpPort: Int
		get() = this.checkProperty(KEY_MAIL_SMTP_PORT).let {
			it.toIntOrNull() ?: throw RuntimeException("The smtp port “$it” is not valid")
		}

	val mailSmtpHost: String
		get() = this.checkProperty(KEY_MAIL_SMTP_HOST)

	val mailConfirmationSubject: String
		get() = this.checkProperty(KEY_MAIL_CONFIRMATION_SUBJECT)

	val mailConfirmationFrom: String
		get() = this.checkProperty(KEY_MAIL_CONFIRMATION_FROM)

	val mailConfirmationMimeSubtype: String
		get() = this.checkProperty(KEY_MAIL_CONFIRMATION_MIME_SUBTYPE)

	val confirmationMailTemplate: String
		get() = confirmationMailText

	val publicUrl: String
		get() = this.checkProperty(KEY_PUBLIC_URL)

	companion object {
		const val KEY_PORT = "port"
		const val KEY_PUBLIC_URL = "public.url"
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
		const val KEY_MAIL_SMTP_HOST = "mail.smtp.host"
		const val KEY_MAIL_SMTP_PORT = "mail.smtp.port"
		const val KEY_MAIL_CONFIRMATION_SUBJECT = "mail.confirmation.subject"
		const val KEY_MAIL_CONFIRMATION_FROM = "mail.confirmation.from"
		const val KEY_MAIL_CONFIRMATION_MIME_SUBTYPE = "mail.confirmation.mime.subtype"
		const val KEY_MAIL_CONFIRMATION_TEMPLATE = "mail.confirmation.template"
	}

	init {
		properties = Properties().apply {
			try {
				Files.newBufferedReader(path).use { r -> this.load(r) }
			} catch (ignored: NoSuchFileException) {
				throw RuntimeException("couldn't find configuration file “$path”")
			}
		}
		val templatePathProperty = this.checkProperty(KEY_MAIL_CONFIRMATION_TEMPLATE)
		try {
			val templatePath = Paths.get(templatePathProperty)
			if (!Files.isReadable(templatePath)) {
				throw RuntimeException("The template at path “$templatePathProperty” does not exist or is not readable.")
			}
			confirmationMailText = String(Files.readAllBytes(templatePath), Charset.forName("utf-8"))
		} catch (ignored: InvalidPathException) {
			throw RuntimeException("The template path “$templatePathProperty” is not valid")
		}
	}
}
