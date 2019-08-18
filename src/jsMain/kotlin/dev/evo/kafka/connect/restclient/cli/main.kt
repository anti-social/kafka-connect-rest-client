package company.evo.kafka.connect.restclient.cli

import io.ktor.client.engine.js.Js

@JsModule("process")
external object process {
    val argv: Array<String>
}

//suspend fun main() {
//    val args = process.argv.sliceArray(2 until process.argv.size)
//    run(args, Js.create {})
//}
