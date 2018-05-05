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
package de.plapadoo.bote.database.impl

import de.plapadoo.bote.database.api.Database
import de.plapadoo.bote.database.api.Database.Companion.DB_DATE_PATTERN
import de.plapadoo.bote.database.model.tables.Subscriber.SUBSCRIBER
import io.reactivex.Completable
import io.reactivex.Single
import org.jooq.DSLContext
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class JOOQDatabase(url: String, userName: String?, password: String?) : Database {
	private val connection: Connection

	override fun createSubscriber(email: String): Single<String?> {
		return Single.fromCallable {
			val subscriber = context().newRecord(SUBSCRIBER)
			subscriber.email = email
			subscriber.token = UUID.randomUUID().toString()
			if (subscriber.store() == 1) subscriber.token else null
		}
	}

	override fun deleteSubscriber(email: String): Completable {
		return Completable.fromRunnable {
			context().deleteFrom(SUBSCRIBER)
					.where(SUBSCRIBER.EMAIL.eq(email))
					.execute()
		}
	}

	override fun deleteUnconfirmedSubscribers(): Completable {
		return Completable.fromRunnable {
			val deleteBefore = Instant
					.now()
					.minus(24, ChronoUnit.HOURS)
					.atOffset(ZoneOffset.UTC)
					.format(DateTimeFormatter.ofPattern(DB_DATE_PATTERN))
			context().deleteFrom(SUBSCRIBER)
					.where(SUBSCRIBER.CREATED.ge(deleteBefore).not().and(SUBSCRIBER.TOKEN.isNotNull))
					.execute()
		}
	}

	override fun confirmSubscriber(token: String): Single<Boolean> {
		return Single.fromCallable {
			context().update(SUBSCRIBER)
					.set(SUBSCRIBER.TOKEN, null as String?)
					.where(SUBSCRIBER.TOKEN.eq(token))
					.execute() == 1
		}
	}


	private fun context(): DSLContext {
		return DSL.using(this.connection,	Settings().withRenderSchema(false));
	}

	@Throws(Exception::class) override fun close() {
		this.connection.close()
	}

	init {
		try {
			this.connection = DriverManager.getConnection(url, userName, password)
		} catch (e: SQLException) {
			throw RuntimeException(e)
		}
	}

}
