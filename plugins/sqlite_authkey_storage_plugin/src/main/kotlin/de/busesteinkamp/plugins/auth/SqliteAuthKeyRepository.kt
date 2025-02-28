package de.busesteinkamp.plugins.auth

import de.busesteinkamp.domain.auth.AuthKey
import de.busesteinkamp.domain.auth.AuthKeyRepository
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

class SqliteAuthKeyRepository : AuthKeyRepository {
    private val url = "jdbc:sqlite:auth_keys.db"

    init {
        Class.forName("org.sqlite.JDBC")
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
          DriverManager.getConnection(url).use { connection ->
              connection.createStatement().use { statement ->
                  statement.executeUpdate(
                      "CREATE TABLE IF NOT EXISTS auth_keys (platformName TEXT PRIMARY KEY, authKey TEXT, createdAt TEXT, expiresAt TEXT)"
                  )
              }
          }
    }

    override fun find(platformName: String): AuthKey? {
        val sql = "SELECT authKey, createdAt, expiresAt FROM auth_keys WHERE platformName = ?"
        DriverManager.getConnection(url).use { connection ->
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, platformName)
                val resultSet = preparedStatement.executeQuery()
                return if (resultSet.next()) {
                    AuthKey(
                        platformName,
                        resultSet.getString("authKey"),
                        parseDate(resultSet.getString("createdAt")),
                        parseDate(resultSet.getString("expiresAt"))
                    )
                } else {
                    null
                }
            }
        }
    }

    override fun save(authKey: AuthKey): AuthKey {
        val sql = "INSERT INTO auth_keys (platformName, authKey, createdAt, expiresAt) VALUES (?, ?, ?, ?)"
        DriverManager.getConnection(url).use { connection ->
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, authKey.platformName)
                preparedStatement.setString(2, authKey.key)
                preparedStatement.setDate(3, java.sql.Date(authKey.createdAt.time))
                preparedStatement.setDate(4, java.sql.Date(authKey.expiresAt.time))
                preparedStatement.executeUpdate()
            }
        }
        return authKey
    }

    override fun update(authKey: AuthKey): AuthKey {
        val sql = "UPDATE auth_keys SET authKey = ?, createdAt = ?, expiresAt = ? WHERE platformName = ?"
        DriverManager.getConnection(url).use { connection ->
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, authKey.key)
                preparedStatement.setDate(2, java.sql.Date(authKey.createdAt.time))
                preparedStatement.setDate(3, java.sql.Date(authKey.expiresAt.time))
                preparedStatement.setString(4, authKey.platformName)
                preparedStatement.executeUpdate()
            }
        }
        return authKey
    }

    override fun delete(platformName: String) {
        val sql = "DELETE FROM auth_keys WHERE platformName = ?"
        DriverManager.getConnection(url).use { connection ->
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, platformName)
                preparedStatement.executeUpdate()
            }
        }
    }

    private fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        return dateFormat.parse(dateString)
    }
}