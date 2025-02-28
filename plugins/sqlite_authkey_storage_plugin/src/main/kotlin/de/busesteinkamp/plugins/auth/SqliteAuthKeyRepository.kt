package de.busesteinkamp.plugins.auth

import de.busesteinkamp.domain.auth.AuthKey
import de.busesteinkamp.domain.auth.AuthKeyRepository
import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.util.*

class SqliteAuthKeyRepository : AuthKeyRepository {
    private val url = "jdbc:sqlite:auth_keys.db"

    init {
        Class.forName("org.sqlite.JDBC")
        createTableIfNotExists()
      }
      private fun createTableIfNotExists() {
        try (val connection = DriverManager.getConnection(url);
             val statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS auth_keys (platformName TEXT PRIMARY KEY, authKey TEXT, createdAt TEXT, expiresAt TEXT)"
            )
        } catch (e: SQLException) {
            throw RuntimeException("Error creating table", e)
        }
    }
        
        
    }

    override fun find(platformName: String): AuthKey? {
        val sql = "SELECT authKey, createdAt, expiresAt FROM auth_keys WHERE platformName = ?"
        try (val connection = DriverManager.getConnection(url);
             val preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, platformName)
            val resultSet = preparedStatement.executeQuery()
            return if (resultSet.next()) {
                AuthKey(platformName, resultSet.getString("authKey"), parseDate(resultSet.getString("createdAt")), parseDate(resultSet.getString("expiresAt")))
            } else {
                null
            }
        } catch (e: SQLException) {
            throw RuntimeException("Error finding auth key", e)
        }
    }

    override fun save(authKey: AuthKey): AuthKey {
        val connection = DriverManager.getConnection(url)
        val statement = connection.createStatement()
        statement.executeUpdate(
            "INSERT INTO auth_keys (authKey, platformName, createdAt, expiresAt) VALUES ('${authKey.key}', '${authKey.platformName}', '${authKey.createdAt}', '${authKey.expiresAt}')"
        )
        statement.close()
        connection.close()
        return authKey
    }

    override fun update(authKey: AuthKey): AuthKey {
        val connection = DriverManager.getConnection(url)
        val statement = connection.createStatement()
        statement.executeUpdate(
            "UPDATE auth_keys SET authKey = '${authKey.key}', createdAt = '${authKey.createdAt}', expiresAt = '${authKey.expiresAt}' WHERE platformName = '${authKey.platformName}'"
        )
        statement.close()
        connection.close()
        return authKey
    }

    override fun delete(platformName: String) {
        val connection = DriverManager.getConnection(url)
        val statement = connection.createStatement()
        statement.executeUpdate(
            "DELETE FROM auth_keys WHERE platformName = '$platformName'"
        )
        statement.close()
        connection.close()
    }

    private fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        return dateFormat.parse(dateString)
    }
}