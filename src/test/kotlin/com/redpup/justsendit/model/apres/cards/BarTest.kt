package com.redpup.justsendit.model.apres.cards

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.google.inject.Inject
import com.redpup.justsendit.control.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.random.testing.FakeRandom
import com.redpup.justsendit.model.random.testing.FakeRandomModule
import com.redpup.justsendit.model.skill.SkillFactory
import com.redpup.justsendit.model.skill.testing.FakeSkillModule
import com.redpup.justsendit.model.supply.proto.skillCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class BarTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @Inject private lateinit var skillFactory: SkillFactory
  @Inject private lateinit var random: FakeRandom

  @BeforeEach
  fun setUp() {
    Guice.createInjector(FakeSkillModule(), FakeRandomModule()).injectMembers(this)
    player = MutablePlayer(handler)
  }

  private val bar = Bar(apresCard { name = "Bar" })

  @Test
  fun `first player reveals 6 cards`() {
    repeat(7) { player.skillDeck.add(skillFactory.create(skillCard { name = "Card $it" })) }
    runBlocking { bar.apply(player, true, gameModel, random) }
    assertThat(player.points).isEqualTo(12) // 6 cards * 2 points
  }

  @Test
  fun `other player reveals 3 cards`() {
    repeat(7) { player.skillDeck.add(skillFactory.create(skillCard { name = "Card $it" })) }
    runBlocking { bar.apply(player, false, gameModel, random) }
    assertThat(player.points).isEqualTo(6) // 3 cards * 2 points
  }
}
