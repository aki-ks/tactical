package me.aki.tactical.core.parser.test

import java.util.Optional

import scala.collection.JavaConverters._
import me.aki.tactical.core.{FieldRef, MethodRef, Path}
import me.aki.tactical.core.`type`._
import me.aki.tactical.core.constant._
import me.aki.tactical.core.handle.{InvokeInterfaceHandle, InvokeStaticHandle, SetStaticHandle}
import me.aki.tactical.core.parser._
import me.aki.tactical.core.textify.ConstantTextifier
import org.scalatest.prop.PropertyChecks
import org.scalatest._

class ConstantTest extends FlatSpec with Matchers with PropertyChecks {
  "The FieldConstantParser" should "parse int values" in {
    forAll { int: Int =>
      FieldConstantParser.parse(int.toString) shouldEqual new IntConstant(int)
    }
  }

  it should "parse long values" in {
    forAll { long: Long =>
      FieldConstantParser.parse(long + "l") shouldEqual new LongConstant(long)
      FieldConstantParser.parse(long + "L") shouldEqual new LongConstant(long)
    }
  }

  it should "parse float values" in {
    forAll { float: Float =>
      FieldConstantParser.parse(float + "f") shouldEqual new FloatConstant(float)
      FieldConstantParser.parse(float + "F") shouldEqual new FloatConstant(float)
    }
  }

  it should "parse double values" in {
    forAll { double: Double =>
      FieldConstantParser.parse(double + "d") shouldEqual new DoubleConstant(double)
      FieldConstantParser.parse(double + "D") shouldEqual new DoubleConstant(double)
    }
  }

  it should "parse string values" in {
    forAll { string: String =>
      val escapedString = string.replace("\"", "\\\"")
      FieldConstantParser.parse('"' + escapedString + '"') shouldEqual new StringConstant(string)
    }
  }

  it should "parse all kinds of generated textified values" in {
    generatorTest(CoreGenerator.fieldConstant, FieldConstantParser, ConstantTextifier.FIELD)
  }

  "The PushableConstantParser" should "parse 'null' constants" in {
    PushableConstantParser.parse("null") shouldEqual NullConstant.getInstance
  }

  it should "parse class constants" in {
    PushableConstantParser.parse("java.lang.String.class") shouldEqual new ClassConstant(new ObjectType(Path.STRING))
    PushableConstantParser.parse("java.lang.String[][].class") shouldEqual new ClassConstant(new ArrayType(new ObjectType(Path.STRING), 2))
  }

  it should "parse method type constants" in {
    PushableConstantParser.parse("method { () void }") shouldEqual new MethodTypeConstant(Nil.asJava, Optional.empty[Type])
    PushableConstantParser.parse("method { (int, long) double }") shouldEqual new MethodTypeConstant(List[Type](IntType.getInstance, LongType.getInstance).asJava, Optional.of(DoubleType.getInstance))
  }

  it should "parse handle constants" in {
    PushableConstantParser.parse("handle { set static java.lang.System.out : java.io.PrintStream }") shouldEqual
      new HandleConstant(new SetStaticHandle(new FieldRef(Path.of("java", "lang", "System"), "out", new ObjectType(Path.of("java", "io", "PrintStream")))))

    PushableConstantParser.parse("handle { invoke interface java.io.PrintStream.println(int) : void }") shouldEqual
      new HandleConstant(new InvokeInterfaceHandle(new MethodRef(Path.of("java", "io", "PrintStream"), "println", List[Type](IntType.getInstance).asJava, Optional.empty[Type])))
  }

  it should "parse dynamic constants" in {
    val constant = PushableConstantParser.parse(
      """dynamic {
        |  name = "foo",
        |  type = int,
        |  bootstrap = invoke static java.lang.Integer.parseInt(java.lang.String) : int,
        |  arguments = [ 10, 20, "bar"]
        |}
      """.stripMargin.trim)

    val bootstrapMethod = new MethodRef(Path.of("java", "lang", "Integer"), "parseInt", List[Type](new ObjectType(Path.STRING)).asJava, Optional.of(IntType.getInstance))
    val arguments = List[BootstrapConstant](new IntConstant(10), new IntConstant(20), new StringConstant("bar"))
    constant shouldEqual new DynamicConstant("foo", IntType.getInstance, new InvokeStaticHandle(bootstrapMethod, false), arguments.asJava)
  }

  it should "parse all kinds of generated textified values" in {
    generatorTest(CoreGenerator.pushableConstant, PushableConstantParser, ConstantTextifier.PUSHABLE)
  }

  "The BootstrapConstantParser" should "parse all kinds of generated textified values" in {
    generatorTest(CoreGenerator.bootstrapConstant, BootstrapConstantParser, ConstantTextifier.BOOTSTRAP)
  }

  "The MethodTypeConstantParser" should "parse all kinds of generated textified values" in {
    generatorTest(CoreGenerator.methodTypeConstant, MethodTypeConstantParser, ConstantTextifier.METHOD_TYPE)
  }

  "The DynamicConstantParser" should "parse all kinds of generated textified values" in {
    generatorTest(CoreGenerator.dynamicConstant, DynamicConstantParser, ConstantTextifier.DYNAMIC)
  }
}
