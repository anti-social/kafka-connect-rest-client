package cli

import kotlin.system.exitProcess

import company.evo.kafka.connect.restclient.ConnectInfo

fun main(args: Array<String>) {
    val connectInfo = ConnectInfo("1.0", "987123")
    println(connectInfo)

    exitProcess(0)
}
