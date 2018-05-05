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
package de.plapadoo.bote.database.api

import io.reactivex.Completable
import io.reactivex.Single

interface Database : AutoCloseable {

	companion object {
		const val DB_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"
	}

	fun createSubscriber(email: String): Single<String>

	fun deleteSubscriber(email: String): Completable

	fun deleteUnconfirmedSubscribers(): Completable

	fun confirmSubscriber(token: String): Single<Boolean>
}
