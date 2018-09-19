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

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.Okio
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.*
import java.util.stream.Collectors


data class Configuration(
		val port: Int = 8081,
		val publicUrl: URI,
		val logConfig: Path = Paths.get("logback-config.xml"),
		val dbUrl: URI,
		val dbUsername: String?,
		val dbPassword: String?,
		val languages: List<Language>,
		val mailSmtpHost: String,
		val mailSmtpPort: Int = 587,
		val mailSmtpAuth: Boolean = true,
		val mailSmtpStarttls: Boolean = true,
		val mailUser: String,
		val mailPassword: String,
		val mailConfirmationMimeSubtype: String = "html"
)

data class Language(
		val language: String,
		val confirmSuccessUrl: URI,
		val confirmFailureUrl: URI,
		val unsubscribeSuccessUrl: URI,
		val unsubscribeFailureUrl: URI,
		val subscribeSuccessUrl: URI,
		val subscribeAlreadySubscribedUrl: URI,
		val subscribeFailureUrl: URI,
		val mailConfirmationSubject: String,
		val mailConfirmationFrom: String,
		val mailConfirmationTemplate: Path
)

class ApplicationConfiguration(path: Path) {
	private val confirmationMailTexts: Map<String, String>
	private val config: Configuration

	val port: Int
		get() = this.config.port

	val logConfigPath: Path
		get() {
			val path = this.config.logConfig
			try {
				return if (!Files.isReadable(path)) {
					throw RuntimeException("The log config at path “$path” does not exist or is not readable.")
				} else path
			} catch (ignored: InvalidPathException) {
				throw RuntimeException("The log config path “$path” is not valid")
			}
		}

	val databaseUrl: URI
		get() = this.config.dbUrl

	val databaseUsername: String?
		get() = this.config.dbUsername

	val databasePassword: String?
		get() = this.config.dbPassword

	fun confirmSuccessUrl(language: String): URI? {
		return this.config.languages.find { l -> l.language == language }?.confirmSuccessUrl
	}

	fun confirmFailureUrl(language: String): URI? {
		return this.config.languages.find { l -> l.language == language }?.confirmFailureUrl
	}

	fun unsubscribeSuccessUrl(language: String): URI? {
		return this.config.languages.find { l -> l.language == language }?.unsubscribeSuccessUrl
	}

	fun unsubscribeFailureUrl(language: String): URI? {
		return this.config.languages.find { l -> l.language == language }?.unsubscribeFailureUrl
	}

	fun subscribeSuccessUrl(language: String): URI? {
		return this.config.languages.find { l -> l.language == language }?.subscribeSuccessUrl
	}

	fun subscribeFailureUrl(language: String): URI? {
		return this.config.languages.find { l -> l.language == language }?.subscribeFailureUrl
	}

	fun subscribeAlreadySubscribedUrl(language: String): URI? {
		return this.config.languages.find { l -> l.language == language }?.subscribeAlreadySubscribedUrl
	}

	val mailSmtpPort: Int
		get() = this.config.mailSmtpPort

	val mailSmtpHost: String
		get() = this.config.mailSmtpHost

	val mailSmtpAuth: Boolean
		get() = this.config.mailSmtpAuth

	val mailSmtpStarttls: Boolean
		get() = this.config.mailSmtpStarttls

	val mailUsername: String
		get() = this.config.mailUser

	val mailPassword: String
		get() = this.config.mailPassword

	fun mailConfirmationSubject(language: String): String? {
		return this.config.languages.find { l -> l.language == language }?.mailConfirmationSubject
	}

	fun mailConfirmationFrom(language: String): String? {
		return this.config.languages.find { l -> l.language == language }?.mailConfirmationFrom
	}

	val mailConfirmationMimeSubtype: String
		get() = this.config.mailConfirmationMimeSubtype

	fun confirmationMailTemplate(language: String): String? {
		return this.confirmationMailTexts[language]
	}

	val publicUrl: URI
		get() = this.config.publicUrl

	init {
		val moshi = Moshi.Builder()
				.add(KotlinJsonAdapterFactory())
				.add(PathAdapter())
				.add(URIAdapter())
				.build()
		try {
			config = moshi.adapter(Configuration::class.java).fromJson(Okio.buffer(Okio.source(path)))!!
		} catch (ignored: NoSuchFileException) {
			throw RuntimeException("couldn't find configuration file “$path”")
		}

		this.confirmationMailTexts = this.config.languages.stream().collect(Collectors.toMap({ it.language }, {
			val templatePath = it.mailConfirmationTemplate
			try {
				if (!Files.isReadable(templatePath)) {
					throw RuntimeException("The template at path “$templatePath” does not exist or is not readable.")
				}
				String(Files.readAllBytes(templatePath), Charset.forName("utf-8"))
			} catch (ignored: InvalidPathException) {
				throw RuntimeException("The template path “$templatePath” is not valid")
			}
		}))
	}
}


class PathAdapter {
	@ToJson
	fun toJson(path: Path): String {
		return path.toString()
	}

	@FromJson
	fun fromJson(path: String): Path {
		return Paths.get(path)
	}
}

class URIAdapter {
	@ToJson
	fun toJson(uri: URI): String {
		return uri.toString()
	}

	@FromJson
	fun fromJson(uri: String): URI {
		return URI(uri)
	}
}