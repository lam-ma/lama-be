package com.lama.service

import com.lama.Game
import com.lama.Player
import com.lama.PlayerId
import com.lama.domain.GameStateMessage
import com.lama.domain.ServerMessage

interface PlayerGateway {
    fun send(playerId: PlayerId, message: ServerMessage)
}
