package com.redpup.justsendit.util

import com.google.protobuf.Message
import com.google.protobuf.TextFormat
import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.model.random.Random.Companion.shuffle
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/** Access to file list of protos from a file. */
interface TextProtoReader<T> {
  /** Returns the list of elements in the file. */
  operator fun invoke(): List<T>
}

/** Mutable access to file list of protos from a file. */
interface TextProtoReaderWriter<T> {
  /** Returns the list of elements in the file. */
  operator fun invoke(): List<T>

  /** Updates the list of elements in the file (writes back). */
  fun update(transform: (T) -> T)

  /** Updates the list of elements in the file (writes back). */
  fun updateAll(transform: (List<T>) -> List<T>)
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
  private val shuffler: Random? = null,
) : TextProtoReader<T> {
  private val elements: List<T> by lazy {
    val builder = builder()
    TextFormat.merge(Files.readString(Path.of(path)), builder)
    val elements = builder.get()
    if (shuffler != null) {
      elements.shuffle(shuffler)
    }
    elements
  }

  override fun invoke(): List<T> = elements
}

/**
 * Impl of [TextProtoReaderWriter]
 * @param B - The top level builder type
 * @param T - The element type.
 */
class TextProtoReaderWriterImpl<T, B : Message.Builder>(
  private val path: String,
  private val builder: () -> B,
  get: B.() -> List<T>,
  private val addAll: B.(List<T>) -> B,
) : TextProtoReaderWriter<T> {
  private val reader = TextProtoReaderImpl(path, builder, get)

  override fun invoke(): List<T> = reader()

  override fun update(transform: (T) -> T) {
    reader().map { transform(it) }
      .let { builder().addAll(it) }
      .let {
        File(path).writeText(TextFormat.printer().printToString(it))
      }
  }

  override fun updateAll(transform: (List<T>) -> List<T>) {
    transform(reader())
      .let { builder().addAll(it) }
      .let {
        File(path).writeText(TextFormat.printer().printToString(it))
      }
  }
}
