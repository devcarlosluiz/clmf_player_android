package com.clmf.player.data.repository

import com.clmf.player.data.local.SecureStorage
import com.clmf.player.data.local.dao.ConnectionDao
import com.clmf.player.data.local.entity.ConnectionEntity
import com.clmf.player.domain.model.Connection
import com.clmf.player.domain.model.ConnectionType
import com.clmf.player.domain.repository.ConnectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepositoryImpl @Inject constructor(
    private val dao: ConnectionDao,
    private val secureStorage: SecureStorage
) : ConnectionRepository {

    override fun observeConnections(): Flow<List<Connection>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getSelectedConnection(): Connection? = dao.getSelected()?.toDomain()

    override suspend fun saveConnection(connection: Connection): Long {
        val placeholder = ConnectionEntity(
            id = connection.id,
            name = connection.name,
            type = connection.type.name,
            serverUrl = connection.serverUrl.trimEnd('/'),
            username = connection.username,
            encryptedPassword = "",
            playlistUrl = connection.playlistUrl,
            isSelected = true
        )
        val id = dao.insert(placeholder)
        val reference = if (connection.password.isNotEmpty()) secureStorage.encryptPassword(id, connection.password) else ""
        dao.insert(placeholder.copy(id = id, encryptedPassword = reference))
        dao.selectOnly(id)
        return id
    }

    override suspend fun deleteConnection(connectionId: Long) {
        dao.delete(connectionId)
    }

    override suspend fun selectConnection(connectionId: Long) {
        dao.selectOnly(connectionId)
    }

    private fun ConnectionEntity.toDomain() = Connection(
        id = id,
        name = name,
        type = runCatching { ConnectionType.valueOf(type) }.getOrDefault(ConnectionType.XTREAM),
        serverUrl = serverUrl,
        username = username,
        password = if (encryptedPassword.isNotEmpty()) secureStorage.decryptPassword(encryptedPassword) else "",
        playlistUrl = playlistUrl,
        isSelected = isSelected
    )
}
