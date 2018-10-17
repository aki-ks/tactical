package me.aki.tactical.core.parser

import fastparse.all._
import me.aki.tactical.core._
import me.aki.tactical.core.`type`.Type

package object test {
  object DummyBodyParser extends BodyParser {
    object DummyCtx
    override type Ctx = DummyCtx.type

    def staticInitializerCtx = DummyCtx
    def parameterParser: P[(Ctx, List[Type])] = for(_ ← Pass) yield (DummyCtx, Nil)
    def bodyParser(method: Method, ctx: Ctx): P[Body] = for (_ ← Pass) yield new Body {}
  }
}
