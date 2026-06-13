package com.redpup.justsendit.model.apres.cards

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.google.inject.Inject
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.model.random.testing.FakeRandom
import com.redpup.justsendit.model.random.testing.FakeRandomModule
import com.redpup.justsendit.model.skill.SkillFactory
import com.redpup.justsendit.model.skill.testing.FakeSkillModule
import com.redpup.justsendit.model.supply.proto.skillCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class DiningTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @Inject private lateinit var skillFactory: SkillFactory
  @Inject private lateinit var random: Random

  @BeforeEach
  fun setUp() {
    Guice.createInjector(FakeSkillModule(), FakeRandomModule()).injectMembers(this)
    player = MutablePlayer(handler)
  }

  private val dining = Dining(apresCard { name = "Dining" })

  @Test
  fun `first player gets points for pairs`() {
    val card1 = skillFactory.create(skillCard { name = "1" })
    val card2 = skillFactory.create(skillCard { name = "2" })
    val card3 = skillFactory.create(skillCard { name = "3" })
    player.skillDiscard.addAll(listOf(card1, card1, card2, card2, card2, card3))
    runBlocking { dining.apply(player, true, gameModel, random) }
    assertThat(player.points).isEqualTo(14) // 2 pairs * 7
  }

  @Test
  fun `other player gets points for pairs`() {
    val card1 = skillFactory.create(skillCard { name = "1" })
    val card2 = skillFactory.create(skillCard { name = "2" })
    val card3 = skillFactory.create(skillCard { name = "3" })
    player.skillDiscard.addAll(listOf(card1, card1, card2, card2, card2, card3))
    runBlocking { dining.apply(player, false, gameModel, random) }
    assertThat(player.points).isEqualTo(8) // 2 pairs * 4
  }
}
