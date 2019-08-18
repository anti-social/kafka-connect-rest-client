package dev.evo.kafka.connect.restclient

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ConnectInfo(
    val version: String,
    val commit: String
)

@Serializable
data class ConnectorStatus(
    val name: String,
    val type: String? = null,
    val connector: Connector,
    val tasks: List<Task>
) {
    @Serializable
    data class Connector(
        val state: State,
        @SerialName("worker_id")
        val workerId: String
    )

    enum class State {
        UNASSIGNED, RUNNING, PAUSED, FAILED
    }

    @Serializable
    data class Task(
        val id: Int,
        @SerialName("worker_id")
        val workerId: String,
        val state: State,
        val trace: String? = null
    )
}
