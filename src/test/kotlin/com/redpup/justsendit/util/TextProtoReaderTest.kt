package com.redpup.justsendit.util

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.proto.Test
import java.io.File
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test as JUnitTest
import org.junit.jupiter.api.io.TempDir

class TextProtoReaderTest {

  @TempDir
  lateinit var tempDir: File

  private lateinit var testFile: File

  @BeforeEach
  fun setup() {
    testFile = File(tempDir, "test.textproto")
  }

  @JUnitTest
  fun `reads single element correctly`() {
    testFile.writeText("value: 42")
    val reader = TextProtoReaderImpl(
      testFile.absolutePath,
      Test::newBuilder,
      { listOf(this.build()) },
      shuffle = false
    )

    val result = reader()
    assertThat(result.size).isEqualTo(1)
    assertThat(result[0].value).isEqualTo(42)
  }

  @JUnitTest
  fun `reads multiple elements correctly`() {
    // Note: TextFormat for a single message doesn't directly support multiple messages.
    // The typical pattern is to have a top-level message that contains a 'repeated' field.
    // Let's simulate this by having a repeated field in a wrapper message, although test.proto doesn't have one.
    // We will use a fake 'get' lambda to simulate getting a list.
    // Let's assume we have a proto like `message TestList { repeated Test tests = 1; }`
    // And the textproto is `tests: { value: 1 } tests: { value: 2 }`
    // We can't do that. So we adjust the test. `get` is `B.() -> List<T>`. So we can use a real list proto.
    // But we don't have one for Test. We can use MountainTileList with a single tile.

    // The class under test is `TextProtoReaderImpl`. The `get` function is `B.() -> List<T>`.
    // It's designed to extract a repeated field from a top-level message.
    // For `Test` message, it does not have a repeated field.
    // Let's see `GameModel.kt` how it is used.
    // TextProtoReaderImpl(tilesPath, MountainTileList::newBuilder, MountainTileList.Builder::getTilesList, shuffle = true)
    // MountainTileList has `repeated MountainTile tiles = 1;`.
    // The `get` lambda is `MountainTileList.Builder::getTilesList`. This returns `MutableList<MountainTile>`.

    // So to test multiple elements, I need a message with a repeated field.
    // I can't use `Test`. I will use `PlayerCardList` from `player.proto`.

    val protoText = """
    player {
      name: "Player A"
    }
    player {
      name: "Player B"
    }
    """.trimIndent()
    testFile.writeText(protoText)

    val reader = com.redpup.justsendit.model.player.proto.PlayerCardList.newBuilder()
    com.google.protobuf.TextFormat.merge(protoText, reader)


    val listReader = TextProtoReaderImpl(
      testFile.absolutePath,
      com.redpup.justsendit.model.player.proto.PlayerCardList::newBuilder,
      com.redpup.justsendit.model.player.proto.PlayerCardList.Builder::getPlayerList
    )

    val result = listReader()
    assertThat(result.size).isEqualTo(2)
    assertThat(result[0].name).isEqualTo("Player A")
    assertThat(result[1].name).isEqualTo("Player B")
  }

  @JUnitTest
  fun `shuffling works as expected`() {
    val protoText = """
    player { name: "P1" }
    player { name: "P2" }
    player { name: "P3" }
    player { name: "P4" }
    player { name: "P5" }
    """.trimIndent()
    testFile.writeText(protoText)

    val readerNoShuffle = TextProtoReaderImpl(
      testFile.absolutePath,
      com.redpup.justsendit.model.player.proto.PlayerCardList::newBuilder,
      com.redpup.justsendit.model.player.proto.PlayerCardList.Builder::getPlayerList,
      shuffle = false
    )

    val readerShuffle = TextProtoReaderImpl(
      testFile.absolutePath,
      com.redpup.justsendit.model.player.proto.PlayerCardList::newBuilder,
      com.redpup.justsendit.model.player.proto.PlayerCardList.Builder::getPlayerList,
      shuffle = true
    )

    val resultNoShuffle = readerNoShuffle()
    val resultShuffle = readerShuffle()

    assertThat(resultNoShuffle.size).isEqualTo(5)
    assertThat(resultShuffle.size).isEqualTo(5)

    // The shuffled list *could* be the same, but with 5 elements it's very unlikely.
    // A better test is to check if the elements are the same, but the order is not guaranteed.
    // But what we really want to test is that the shuffle flag is respected.
    // Let's run it multiple times, the shuffled one should change order.
    val firstRead = readerNoShuffle()
    val secondRead = readerNoShuffle()
    assertThat(secondRead.map { it.name }).isEqualTo(firstRead.map { it.name })

    val firstShuffleRead = readerShuffle()
    val secondShuffleRead = readerShuffle()
    // The reader implementation has `by lazy`, so it will only be read and shuffled once.
    // So `firstShuffleRead` and `secondShuffleRead` will be identical.

    // The best we can do is to compare the shuffled and non-shuffled list.
    assertNotEquals(resultNoShuffle.map { it.name }, resultShuffle.map { it.name })
  }
}
