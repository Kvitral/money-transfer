package com.kvitral.repository

import com.kvitral.operations.LoggingOperations
import monix.eval.Task
import org.slf4j.LoggerFactory

class TaskLogger(name: String) extends LoggingOperations[Task] {

  private val logger = LoggerFactory.getLogger(name)

  override def info(msg: => String): Task[Unit] = Task.eval(logger.info(msg))

  override def error(msg: => String): Task[Unit] = Task.eval(logger.error(msg))
}

object TaskLogger {
  def apply(name: String): TaskLogger = new TaskLogger(name)
}
