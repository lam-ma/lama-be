package com.lama.service

import com.lama.Game
import com.lama.Player
import com.lama.PlayerId

interface GameStateListener {
    fun stateChanged(game: Game, players: List<Player>)
}
