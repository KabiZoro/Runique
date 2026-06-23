@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kabi.wear.run.data

import com.kabi.core.connectivity.domain.DeviceNode
import com.kabi.core.connectivity.domain.DeviceType
import com.kabi.core.connectivity.domain.NodeDiscovery
import com.kabi.core.connectivity.domain.messaging.MessagingAction
import com.kabi.core.connectivity.domain.messaging.MessagingClient
import com.kabi.core.connectivity.domain.messaging.MessagingError
import com.kabi.core.domain.util.EmptyResult
import com.kabi.wear.run.domain.PhoneConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn

class WatchToPhoneConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope,
    private val messagingClient: MessagingClient
) : PhoneConnector {

    private val _connectorNode = MutableStateFlow<DeviceNode?>(null)
    override val connectedNode = _connectorNode.asStateFlow()

    override val messagingActions = nodeDiscovery
        .observeConnectedDevices(DeviceType.WATCH)
        .flatMapLatest { connectedNodes ->
            val node = connectedNodes.firstOrNull()
            if (node != null && node.isNearBy) {
                _connectorNode.value = node
                messagingClient.connectToNode(node.id)
            } else flowOf()
        }
        .shareIn(
            applicationScope,
            SharingStarted.Eagerly
        )

    override suspend fun sendActionToPhone(action: MessagingAction): EmptyResult<MessagingError> {
        return messagingClient.sendOrQueueAction(action)
    }
}