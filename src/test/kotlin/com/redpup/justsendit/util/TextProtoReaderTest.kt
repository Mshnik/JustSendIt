package com.redpup.justsendit.util

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.supply.proto.PlayerCardList
import com.redpup.justsendit.proto.TestMessage
import java.io.File
import kotlin.test.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

class TextProtoReaderTest {

  @TempDir
  lateinit var tempDir: File

  private lateinit var testFile: File

  @BeforeEach
  fun setup() {
    testFile = File(tempDir, "test.textproto")
  }

  @Test
  fun `reads single element correctly`() {
    testFile.writeText("value: 42")
    val reader = TextProtoReaderImpl(
      testFile.absolutePath,
      TestMessage::newBuilder,
      { listOf(this.build()) },
    )

    val result = reader()
    assertThat(result.size).isEqualTo(1)
    assertThat(result[0].value).isEqualTo(42)
  }

  @Test
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

    val reader = PlayerCardList.newBuilder()
    com.google.protobuf.TextFormat.merge(protoText, reader)


    val listReader = TextProtoReaderImpl(
      testFile.absolutePath,
      PlayerCardList::newBuilder,
      PlayerCardList.Builder::getPlayerList
    )

    val result = listReader()
    assertThat(result.size).isEqualTo(2)
    assertThat(result[0].name).isEqualTo("Player A")
    assertThat(result[1].name).isEqualTo("Player B")
  }
}
