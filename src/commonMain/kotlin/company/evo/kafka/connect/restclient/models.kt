package company.evo.kafka.connect.restclient

import kotlinx.serialization.Encoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialName
import kotlinx.serialization.internal.StringDescriptor

@Serializable
data class ConnectInfo(
    val version: String,
    val commit: String
)

@Serializable
data class ConnectorStatus(
    val name: String,
    val type: String,
    val connector: Connector,
    val tasks: List<Task>
) {
    @Serializable
    data class Connector(
        // https://github.com/Kotlin/kotlinx.serialization/issues/242
        @Serializable(StateSerializer::class)
        val state: State,
        @SerialName("worker_id")
        val workerId: String
    )

    enum class State {
        UNASSIGNED, RUNNING, PAUSED, FAILED
    }

    @Serializer(forClass = State::class)
    object StateSerializer : KSerializer<State> {
        override val descriptor: SerialDescriptor = StringDescriptor

        override fun serialize(output: Encoder, obj: State) {
            output.encodeString(obj.toString())
        }
        override fun deserialize(input: Decoder): State {
            return State.valueOf(input.decodeString())
        }
    }

    @Serializable
    class Task(
        val id: Int,
        @SerialName("worker_id")
        val workerId: String,
        @Serializable(StateSerializer::class)
        val state: State
    )
}
