package com.redpup.justsendit.util

/** Removes and returns the first element in the list or throws if it is empty.*/
fun <T> MutableList<T>.pop(name: String = "List") =
  removeFirstOrNull() ?: throw NoSuchElementException("No elements in list $name")

/** Applies [fn] to each element in [this] and returns this. */
fun <T, I : Iterable<T>> I.peek(fn: (T) -> Unit): I {
  return also { forEach(fn) }
}

/** Counts the number of duplicates in [this]. */
fun <T, I : Iterable<T>> I.countDuplicates(): Int {
  return groupingBy { it }.eachCount()
    .filter { it.value > 1 }
    .map { it.value * (it.value - 1) / 2 }
    .sum()
}
