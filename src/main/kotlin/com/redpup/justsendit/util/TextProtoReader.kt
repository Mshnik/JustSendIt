package com.redpup.justsendit.util

import com.google.protobuf.Message
import com.google.protobuf.TextFormat
import java.nio.file.Files
import java.nio.file.Path

/** Access to file list of protos from a file. */
interface TextProtoReader<T> {
  /** Returns the list of elements in the file. */
  operator fun invoke(): List<T>
}

/**
 * Impl of [TextProtoReader]
 * @param B - The top level builder type
 * @param T - The element type.
 */
class TextProtoReaderImpl<T, B : Message.Builder>(
  private val path: String,
  private val builder: () -> B,
  private val get: B.() -> List<T>,
  private val shuffle: Boolean = false,
) : TextProtoReader<T> {
  private val elements: List<T> by lazy {
    val builder = builder()
    TextFormat.merge(Files.readString(Path.of(path)), builder)
    val elements = builder.get()
    if (shuffle) elements.shuffled() else elements
  }

  override fun invoke(): List<T> = elements
}