package com.mix.rhino


fun initNIOServer(
        portToListen : Int = 4545,
        block: com.mix.rhino.NetworkService.()-> Unit
) : com.mix.rhino.NetworkService
{
    val networkService = com.mix.rhino.NetworkService(portToListen)
    block(networkService)
    return networkService
}

fun initNIOClient (
        serverPort : Int = 4545,
        serverAddress : String = "127.0.0.1",
        block: com.mix.rhino.ClientNetworkService.()-> Unit
) : com.mix.rhino.ClientNetworkService {

    val clientNetworkService = com.mix.rhino.ClientNetworkService(serverAddress, serverPort)
    block(clientNetworkService)
    return clientNetworkService
}


