package de.plapadoo.bote.database.api

import io.reactivex.Completable
import io.reactivex.Single

interface Database : AutoCloseable {

	companion object {
		const val DB_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"
	}

	fun createSubscriber(email: String): Single<String?>

	fun deleteSubscriber(email: String): Completable

	fun deleteUnconfirmedSubscribers(): Completable

	fun confirmSubscriber(token: String): Single<Boolean>
}
