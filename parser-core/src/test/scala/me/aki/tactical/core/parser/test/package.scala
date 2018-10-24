package me.aki.tactical.core.parser

import java.util.Optional

import fastparse.all._
import me.aki.tactical.core._
import me.aki.tactical.core.`type`.Type
import me.aki.tactical.core.textify.Textifier

import org.scalacheck.Gen

package object test {
  implicit def optionAsJava[A](o: Option[A]) = new {
    def asJava: Optional[A] = o match {
      case Some(value) => Optional.of(value)
      case None => Optional.empty()
    }
  }

  def generatorTest[A](gen: Gen[A], parser: Parser[A], textifier: Textifier[A]): Unit = {
    import org.scalatest.Matchers._
    import org.scalatest.prop.PropertyChecks._

    forAll (gen) { value =>
      val text = textifier.toString(value)
      try {
        val parsed = parser.parse(text)
        value shouldEqual parsed
      } catch {
        case t: Throwable => throw new RuntimeException(s"Error while parsing text '${text}'", t)
      }
    }
  }

  object DummyBodyParser extends BodyParser {
    object DummyCtx
    override type Ctx = DummyCtx.type

    def staticInitializerCtx = DummyCtx
    def parameterParser: P[(Ctx, List[Type])] = for(_ ← Pass) yield (DummyCtx, Nil)
    def bodyParser(method: Method, ctx: Ctx): P[Body] = for (_ ← Pass) yield new Body {}
  }
}
