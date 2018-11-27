package com.kvitral.model

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class SuccessMessage(message: String)

object SuccessMessage {
  implicit val encoder: Encoder[SuccessMessage] = deriveEncoder
  implicit val decoder: Decoder[SuccessMessage] = deriveDecoder
}
