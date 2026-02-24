package hu.bme.aut.android.demo.data.network.api

import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.data.network.model.teamMatch.TeamMatchDTO
import hu.bme.aut.android.demo.data.network.model.team.TeamWithMembersDTO
import hu.bme.aut.android.demo.domain.websocket.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO

interface ApiService {
    suspend fun getPlayers(): List<PlayerDTO>
    suspend fun addPlayer(player: NewPlayerDTO): PlayerDTO
    suspend fun deletePlayer(id: Int)
    suspend fun updatePlayer(id: Int, player: NewPlayerDTO)
    suspend fun registerFcmToken(registration: FcmToken)
    suspend fun sendPushNotification(payload: Map<String, String>)

    //Team
    suspend fun getTeams(): List<TeamWithMembersDTO>


    //TeamMatch
    suspend fun getTeamMatches(): List<TeamMatchDTO>

    //SingleMatch

}