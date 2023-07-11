package org.clulab.fatdynet.utils

class FatDynetException(message: String) extends RuntimeException(message)

class SynchronizationException(message: String) extends FatDynetException(message)
