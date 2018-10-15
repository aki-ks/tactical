package me.aki.tactical.core.parser.test

import scala.collection.JavaConverters._

import java.util.Optional

import me.aki.tactical.core.`type`.{DoubleType, IntType, ObjectType, Type}
import me.aki.tactical.core.handle._
import me.aki.tactical.core.parser.HandleParser
import me.aki.tactical.core.{FieldRef, MethodRef, Path}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class HandleTest extends FlatSpec with Matchers with PropertyChecks {
  val fieldRefs = Table(
    ("textified FieldRef", "FieldRef"),
    ("java.lang.System.out : java.io.PrintStream", new FieldRef(Path.of("java", "lang", "System"), "out", new ObjectType(Path.of("java", "io", "PrintStream")))),
    ("java.lang.Math.PI : double", new FieldRef(Path.of("java", "lang", "Math"), "PI", DoubleType.getInstance))
  )

  val methodRefs = Table(
    ("textifier MethodRef", "MethodRef"),
    ("java.io.PrintStream.println(int) : void", new MethodRef(Path.of("java", "io", "PrintStream"), "println", List[Type](IntType.getInstance).asJava, Optional.empty[Type])),
    ("java.lang.Integer.parseInt(java.lang.String) : int", new MethodRef(Path.of("java", "lang", "Integer"), "parseInt", List[Type](new ObjectType(Path.STRING)).asJava, Optional.of(IntType.getInstance)))
  )

  "The HandleParser" should "parse all kinds of field handles" in {
    forAll (fieldRefs) { (textified, fieldRef) =>
      HandleParser.parse(s"get $textified") shouldEqual new GetFieldHandle(fieldRef)
      HandleParser.parse(s"set $textified") shouldEqual new SetFieldHandle(fieldRef)
      HandleParser.parse(s"get static $textified") shouldEqual new GetStaticHandle(fieldRef)
      HandleParser.parse(s"set static $textified") shouldEqual new SetStaticHandle(fieldRef)
    }
  }

  it should "parse basic method invokes" in {
    forAll (methodRefs) { (textified, methodRef) =>
      HandleParser.parse(s"invoke interface $textified") shouldEqual new InvokeInterfaceHandle(methodRef)
      HandleParser.parse(s"invoke special $textified") shouldEqual new InvokeSpecialHandle(methodRef, false)
      HandleParser.parse(s"invoke static $textified") shouldEqual new InvokeStaticHandle(methodRef, false)
      HandleParser.parse(s"invoke virtual $textified") shouldEqual new InvokeVirtualHandle(methodRef)
      HandleParser.parse(s"invoke new $textified") shouldEqual new NewInstanceHandle(methodRef)
    }
  }

  it should "parse special and static invokes of methods in interfaces" in {
    forAll (methodRefs) { (textified, methodRef) =>
      HandleParser.parse(s"invoke special interface $textified") shouldEqual new InvokeSpecialHandle(methodRef, true)
      HandleParser.parse(s"invoke static interface $textified") shouldEqual new InvokeStaticHandle(methodRef, true)
    }
  }
}
