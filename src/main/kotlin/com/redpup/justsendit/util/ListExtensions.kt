package com.redpup.justsendit.util

/** Removes and returns the first element in the list or throws if it is empty.*/
fun <T> MutableList<T>.pop(name: String = "List") =
  removeFirstOrNull() ?: throw NoSuchElementException("No elements in list $name")