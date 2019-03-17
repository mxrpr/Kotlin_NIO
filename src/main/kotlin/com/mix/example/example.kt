package com.mix.example

import com.mix.rhino.initNIOClient
import com.mix.rhino.initNIOServer
import kotlin.concurrent.thread

fun main() {

    // use default port 4545
    // or use initNIOServer(portNumber) {}
    var receivedMSGNum = 0
    lateinit var networkService: com.mix.rhino.NetworkService

    networkService = initNIOServer {
        registerReceiver { channel, message ->
            run {
                println("Server: message received : $message")
                receivedMSGNum ++
            }
        }
        registerErrorReceiver { errorMessage ->
            run {
                println("Server: error message received : $errorMessage")
            }
        }
        println("Server is starting...")
        thread {
            run()
        }
    }


    // start the client
    val client = initNIOClient {

        registerReceiver { channel, message ->
            run {
                println("Client: message received : $message")
            }
        }

        registerErrorReceiver { errorMessage ->
            run {
                println("Client: error message received : $errorMessage")
            }
        }

        thread {
            run()
        }

        repeat(100) {
            sendMessage("Hello from client")
            println("Client sent msg: $it")
        }

        println("Client has terminated sending + $messageQueueSize")

    }

    while (client.messageQueueSize != 0) {
        Thread.sleep(30)
        println("Message sender queue size: ${client.messageQueueSize}")
    }

    client.terminate()
    networkService.terminate()

}