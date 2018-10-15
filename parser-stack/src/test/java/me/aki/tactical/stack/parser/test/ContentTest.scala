package me.aki.tactical.stack.parser.test

import java.util.Optional

import me.aki.tactical.core.Path
import me.aki.tactical.stack.parser._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class ContentTest extends FlatSpec with Matchers with PropertyChecks with StackCtxTest {
  "The TryCatchBlockParser" should "parse try catch blocks without exception types" in {
    val ctx = newCtx
    val block = new TryCatchBlockParser(ctx).parse("try label1 -> label2 catch label3;")

    block.getExceptionType shouldEqual Optional.empty()
    validateLabels(ctx, Map(
      "label1" -> block.getFirstCell,
      "label2" -> block.getLastCell,
      "label3" -> block.getHandlerCell
    ))
  }

  "The TryCatchBlockParser" should "parse try catch blocks with exception types" in {
    val ctx = newCtx
    val block = new TryCatchBlockParser(ctx).parse("try label1 -> label2 catch label3 : java.lang.Exception;")

    block.getExceptionType shouldEqual Optional.of(Path.of("java", "lang", "Exception"))
    validateLabels(ctx, Map(
      "label1" -> block.getFirstCell,
      "label2" -> block.getLastCell,
      "label3" -> block.getHandlerCell
    ))
  }
}
