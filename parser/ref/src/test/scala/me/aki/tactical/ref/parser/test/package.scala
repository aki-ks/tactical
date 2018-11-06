package me.aki.tactical.ref.parser

import me.aki.tactical.core.textify.{Printer, Textifier}
import me.aki.tactical.ref.textifier.{CtxTextifier, TextifyCtx}

package object test {
  implicit class CtxTextifierFlat[A](textifier: CtxTextifier[A]) {
    def apply(ctx: TextifyCtx) = new Textifier[A] {
      def textify(printer: Printer, value: A): Unit =
        textifier.textify(printer, ctx, value)
    }
  }
}
