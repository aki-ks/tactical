package me.aki.tactical.ref

import fastparse.all._
import me.aki.tactical.core.parser.{Parser, _}

package object parser {
  class LocalParser(ctx: RefCtx) extends Parser[RefLocal] {
    val parser: P[RefLocal] =
      Literal.map(ctx.getLocalOpt)
        .filter(_.isDefined).map(_.get)
  }

  class LabelParser(ctx: ResolvedRefCtx) extends Parser[Statement] {
    val parser: P[Statement] =
      Literal.map(ctx.getLabelOpt)
        .filter(_.isDefined)
        .map(_.get)
  }
}
