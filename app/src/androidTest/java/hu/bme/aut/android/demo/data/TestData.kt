package hu.bme.aut.android.demo.data

import hu.bme.aut.android.demo.data.DataStructure.CaptainTestUser
import hu.bme.aut.android.demo.data.DataStructure.FullTestUser
import hu.bme.aut.android.demo.data.DataStructure.TeamData
import hu.bme.aut.android.demo.data.DataStructure.TeamMatchData

object TestData {

    val teams = listOf(

        // BEAC I.
        TeamData(
            id = 13,
            captain = CaptainTestUser("NZ@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Nagy Zoltán", "nz@test.com", "bestpassword"),
                FullTestUser("Szarvas Tamás", "szt@test.com", "bestpassword"),
                FullTestUser("Bíró Csaba", "bcs@test.com", "bestpassword"),
                FullTestUser("Halász Gábor", "hg@test.com", "bestpassword"),
            ),
            teamName = "BEAC I."
        ),

        // BEAC II.
        TeamData(
            id = 14,
            captain = CaptainTestUser("BG@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Botlik Gergő", "bg@test.com", "bestpassword"),
                FullTestUser("Nagy Tamás", "nt@test.com", "bestpassword"),
                FullTestUser("Váczi Attila", "vam@test.com", "bestpassword"),
                FullTestUser("Simon Dániel", "sd@test.com", "bestpassword"),
            ),
            teamName = "BEAC II."
        ),

        // BEAC III.
        TeamData(
            id = 5,
            captain = CaptainTestUser("tf@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Katus Ferenc", "kf@test.com", "bestpassword"),
                FullTestUser("Szabó Miklós", "szm@test.com", "bestpassword"),
                FullTestUser("Szekulesz Péter", "szp@test.com", "bestpassword"),
                FullTestUser("Bakos Bertalan", "babe@test.com", "bestpassword")
            ),
            teamName = "BEAC III."
        ),
        // BEAC IV.
        TeamData(
            id = 6,
            captain = CaptainTestUser("nzs@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Széles Gergő", "szg@test.com", "bestpassword"),
                FullTestUser("Gulyás Áron", "ga@test.com", "bestpassword"),
                FullTestUser("Gábor Norbert", "gn@test.com", "bestpassword"),
                FullTestUser("Szabó Győző", "szgy@test.com", "bestpassword")
            ),
            teamName = "BEAC IV."
        ),
        // BEAC V.
        TeamData(
            id = 7,
            captain = CaptainTestUser("id@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Kurucz Máté", "km@test.com", "bestpassword"),
                FullTestUser("Juhász Péter", "jp@test.com", "bestpassword"),
                FullTestUser("Wiener Gábor", "wg@test.com", "bestpassword"),
                FullTestUser("Kocsis Vencel", "kv@test.com", "bestpassword")
            ),
            teamName = "BEAC V."
        ),
        // BEAC VI.
        TeamData(
            id = 8,
            captain = CaptainTestUser("kp@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Sipos Tamás", "st@test.com", "bestpassword"),
                FullTestUser("Tengerdi Tibor", "tt@test.com", "bestpassword"),
                FullTestUser("Böröcz Botond", "bb@test.com", "bestpassword"),
                FullTestUser("Reszler Balázs", "rb@test.com", "bestpassword")
            ),
            teamName = "BEAC VI."
        ),

        // MAFC I.
        TeamData(
            id = 15,
            captain = CaptainTestUser("NA@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Nagy Anna", "na@test.com", "bestpassword"),
                FullTestUser("Kovács Bence", "kb@test.com", "bestpassword"),
                FullTestUser("Tóth Dávid", "td@test.com", "bestpassword"),
                FullTestUser("Szabó Eszter", "sze@test.com", "bestpassword"),
            ),
            teamName = "MAFC I."
        ),

        // MAFC II.
        TeamData(
            id = 16,
            captain = CaptainTestUser("HGA@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Horváth Gábor", "hga@test.com", "bestpassword"),
                FullTestUser("Varga Zita", "vz@test.com", "bestpassword"),
                FullTestUser("Kocsis Károly", "kk@test.com", "bestpassword"),
                FullTestUser("Molnár Orsolya", "mo@test.com", "bestpassword"),
            ),
            teamName = "MAFC II."
        ),

        // MAFC III.
        TeamData(
            id = 17,
            captain = CaptainTestUser("NB@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Németh Balázs", "nb@test.com", "bestpassword"),
                FullTestUser("Farkas Kinga", "fk@test.com", "bestpassword"),
                FullTestUser("Balogh Tamás", "bt@test.com", "bestpassword"),
                FullTestUser("Papp Judit", "pj@test.com", "bestpassword"),
            ),
            teamName = "MAFC III."
        ),
        // MAFC IV.
        TeamData(
            id = 18,
            captain = CaptainTestUser("TL@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Takács László", "tl@test.com", "bestpassword"),
                FullTestUser("Juhász Dóra", "jd@test.com", "bestpassword"),
                FullTestUser("Mészáros Zoltán", "mz@test.com", "bestpassword"),
                FullTestUser("Simon Réka", "sr@test.com", "bestpassword"),
            ),
            teamName = "MAFC IV."
        ),
        // MAFC V.
        TeamData(
            id = 19,
            captain = CaptainTestUser("FM@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Fekete Miklós", "fm@test.com", "bestpassword"),
                FullTestUser("Szilágyi Tímea", "szti@test.com", "bestpassword"),
                FullTestUser("Török Gergely", "tg@test.com", "bestpassword"),
                FullTestUser("Fehér Andrea", "fa@test.com", "bestpassword"),
            ),
            teamName = "MAFC V."
        ),
        // MAFC VI.
        TeamData(
            id = 20,
            captain = CaptainTestUser("GD@test.com", "bestpassword"),
            members = listOf(
                FullTestUser("Gál Dániel", "gd@test.com", "bestpassword"),
                FullTestUser("Hegedűs Katalin", "hk@test.com", "bestpassword"),
                FullTestUser("Sipos Márton", "sm@test.com", "bestpassword"),
                FullTestUser("Lukács Boglárka", "lb@test.com", "bestpassword")
            ),
            teamName = "MAFC VI."
        )
    )

    val teamMatches = listOf(
        // --- BEAC I. (teams[0], ID: 13) hazai meccsei ---
        TeamMatchData(id = 35, homeTeam = teams[0], guestTeam = teams[6]),  // vs MAFC I.
        TeamMatchData(id = 36, homeTeam = teams[0], guestTeam = teams[7]),  // vs MAFC II.
        TeamMatchData(id = 37, homeTeam = teams[0], guestTeam = teams[8]),  // vs MAFC III.
        TeamMatchData(id = 38, homeTeam = teams[0], guestTeam = teams[9]),  // vs MAFC IV.
        TeamMatchData(id = 39, homeTeam = teams[0], guestTeam = teams[10]), // vs MAFC V.
        TeamMatchData(id = 40, homeTeam = teams[0], guestTeam = teams[11]), // vs MAFC VI.

        // --- BEAC II. (teams[1], ID: 14) hazai meccsei ---
        TeamMatchData(id = 41, homeTeam = teams[1], guestTeam = teams[6]),
        TeamMatchData(id = 42, homeTeam = teams[1], guestTeam = teams[7]),
        TeamMatchData(id = 43, homeTeam = teams[1], guestTeam = teams[8]),
        TeamMatchData(id = 44, homeTeam = teams[1], guestTeam = teams[9]),
        TeamMatchData(id = 45, homeTeam = teams[1], guestTeam = teams[10]),
        TeamMatchData(id = 46, homeTeam = teams[1], guestTeam = teams[11]),

        // --- BEAC III. (teams[2], ID: 5) hazai meccsei ---
        TeamMatchData(id = 23, homeTeam = teams[2], guestTeam = teams[6]),
        TeamMatchData(id = 24, homeTeam = teams[2], guestTeam = teams[7]),
        TeamMatchData(id = 25, homeTeam = teams[2], guestTeam = teams[8]),
        TeamMatchData(id = 26, homeTeam = teams[2], guestTeam = teams[9]),
        TeamMatchData(id = 27, homeTeam = teams[2], guestTeam = teams[10]),
        TeamMatchData(id = 28, homeTeam = teams[2], guestTeam = teams[11]),

        // --- BEAC IV. (teams[3], ID: 6) hazai meccsei ---
        TeamMatchData(id = 29, homeTeam = teams[3], guestTeam = teams[6]),
        TeamMatchData(id = 30, homeTeam = teams[3], guestTeam = teams[7]),
        TeamMatchData(id = 31, homeTeam = teams[3], guestTeam = teams[8]),
        TeamMatchData(id = 32, homeTeam = teams[3], guestTeam = teams[9]),
        TeamMatchData(id = 33, homeTeam = teams[3], guestTeam = teams[10]),
        TeamMatchData(id = 34, homeTeam = teams[3], guestTeam = teams[11]),

        // --- BEAC V. (teams[4], ID: 7) hazai meccsei ---
        TeamMatchData(id = 11, homeTeam = teams[4], guestTeam = teams[6]),
        TeamMatchData(id = 12, homeTeam = teams[4], guestTeam = teams[7]),
        TeamMatchData(id = 13, homeTeam = teams[4], guestTeam = teams[8]),
        TeamMatchData(id = 14, homeTeam = teams[4], guestTeam = teams[9]),
        TeamMatchData(id = 15, homeTeam = teams[4], guestTeam = teams[10]),
        TeamMatchData(id = 16, homeTeam = teams[4], guestTeam = teams[11]),

        // --- BEAC VI. (teams[5], ID: 8) hazai meccsei ---
        TeamMatchData(id = 17, homeTeam = teams[5], guestTeam = teams[6]),
        TeamMatchData(id = 18, homeTeam = teams[5], guestTeam = teams[7]),
        TeamMatchData(id = 19, homeTeam = teams[5], guestTeam = teams[8]),
        TeamMatchData(id = 20, homeTeam = teams[5], guestTeam = teams[9]),
        TeamMatchData(id = 21, homeTeam = teams[5], guestTeam = teams[10]),
        TeamMatchData(id = 22, homeTeam = teams[5], guestTeam = teams[11])
    )

    /**
     * Valósághű asztalitenisz meccseredmény generátor.
     */
    fun generateRandomSets(): List<Pair<Int, Int>> {
        val sets = mutableListOf<Pair<Int, Int>>()
        var homeWins = 0
        var guestWins = 0

        while (homeWins < 3 && guestWins < 3) {
            val homeWinsSet = (0..1).random() == 1
            val isDeuce = (1..100).random() <= 8
            val homeScore: Int
            val guestScore: Int

            if (isDeuce) {
                val winnerScore = (12..20).random()
                val loserScore = winnerScore - 2

                if (homeWinsSet) {
                    homeScore = winnerScore
                    guestScore = loserScore
                } else {
                    homeScore = loserScore
                    guestScore = winnerScore
                }
            } else {
                val loserScore = (0..9).random()
                if (homeWinsSet) {
                    homeScore = 11
                    guestScore = loserScore
                } else {
                    homeScore = loserScore
                    guestScore = 11
                }
            }

            sets.add(Pair(homeScore, guestScore))
            if (homeWinsSet) homeWins++ else guestWins++
        }
        return sets
    }
}