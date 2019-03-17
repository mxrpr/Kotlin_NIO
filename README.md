# Kotlin NIO library

This is a library to use Java NIO from Kotlin.


# Usage
Check the example in example.kt file.

## Run server
```
    val networkService = initNIOServer {
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
        thread {
            run()
        }
    }
```

In order to be notified when a new content has arrived, a receiver must be registered: 

```
registerReceiver { channel, message ->
            run {
                println("Server: message received : $message")
                receivedMSGNum ++
            }
        }
```

In order to be notified when an error happened, an error receiver must be 
registered: 
```
registerErrorReceiver { errorMessage ->
            run {
                println("Server: error message received : $errorMessage")
            }
        }
```

## Run client 
```
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
```

## Stop the server and client
```
    client.terminate()
    networkService.terminate()
```