package com.mix.rhino

/**
 * Initialise the NIO server. Server responsibility is
 * to open a ServerSocket and listen for connections
 *
 * In order to receive notification about content receiving, an IMessageReceiver
 * must be registered. In case of error, the server will notify the client via
 * IErrorReceiver.
 *
 * @param portToListen Port number where the server will listen
 */
fun initNIOServer(
        portToListen : Int = 4545,
        block: com.mix.rhino.NetworkService.()-> Unit
) : com.mix.rhino.NetworkService
{
    val networkService = com.mix.rhino.NetworkService(portToListen)
    block(networkService)
    return networkService
}

/**
 * Initialise the NIO client. Opens a connection to the server, keeps the
 * channel open. Client application can send message to the server with the
 * help of the `sendMessage` method.
 *
 * In order to receive notification about content receiving, an IMessageReceiver
 * must be registered. In case of error, the server will notify the client via
 * IErrorReceiver.
 *
 * @param serverPort Port number where the server is listening
 * @param serverAddress Address of the server
 */
fun initNIOClient (
        serverPort : Int = 4545,
        serverAddress : String = "127.0.0.1",
        block: com.mix.rhino.ClientNetworkService.()-> Unit
) : com.mix.rhino.ClientNetworkService {

    val clientNetworkService = com.mix.rhino.ClientNetworkService(serverAddress, serverPort)
    block(clientNetworkService)
    return clientNetworkService
}


