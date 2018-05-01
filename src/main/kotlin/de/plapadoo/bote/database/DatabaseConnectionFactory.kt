package de.plapadoo.bote.database

import de.plapadoo.bote.database.api.Database
import de.plapadoo.bote.database.impl.JOOQDatabase

class DatabaseConnectionFactory {
	companion object {
		fun connectToDatabase(url: String, userName: String?, password: String?): Database {
			return JOOQDatabase(url, userName, password)
		}
	}
}