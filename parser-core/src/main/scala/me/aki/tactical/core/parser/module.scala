package me.aki.tactical.core.parser

import scala.collection.JavaConverters._

import java.util.Optional

import fastparse.all._
import me.aki.tactical.core.{ Module, Path }

object ModuleParser extends Parser[Module] {
  val parser: P[Module] = P {
    ModuleFlagParser ~ WS.? ~ "module" ~ WS ~ PathParser ~ WS.? ~ "{" ~ WS.? ~
      ModuleContentParser.rep(sep = WS.?) ~ WS.? ~ "}"
  } map {
    case (flags, name, contents) =>
      val module = new Module(name)
      module.setAccessFlags(flags)
      contents.foreach(_ apply module)
      module
  }
}

trait ModuleContent extends (Module => Unit)
object ModuleContent {
  class Version(version: String) extends ModuleContent {
    override def apply(module: Module): Unit =
      module.setVersion(Optional.of(version))
  }

  class Main(main: Path) extends ModuleContent {
    override def apply(module: Module): Unit =
      module.setMainClass(Optional.of(main))
  }

  class Package(pkg: Path) extends ModuleContent {
    override def apply(module: Module): Unit =
      module.getPackages.add(pkg)
  }

  class Require(require: Module.Require) extends ModuleContent {
    override def apply(module: Module): Unit =
      module.getRequires.add(require)
  }

  class Export(export: Module.Export) extends ModuleContent {
    override def apply(module: Module): Unit =
      module.getExports.add(export)
  }

  class Opens(opens: Module.Open) extends ModuleContent {
    override def apply(module: Module): Unit =
      module.getOpens.add(opens)
  }

  class Use(use: Path) extends ModuleContent {
    override def apply(module: Module): Unit =
      module.getUses.add(use)
  }

  class Provides(provides: Module.Provide) extends ModuleContent {
    override def apply(module: Module): Unit =
      module.getProvides.add(provides)
  }
}

object ModuleContentParser extends Parser[ModuleContent] {
  object VersionParser extends Parser[ModuleContent.Version] {
    val parser: P[ModuleContent.Version] =
      for (version ← "version" ~ WS ~ StringLiteral ~ WS.? ~ ";")
        yield new ModuleContent.Version(version)
  }

  object MainParser extends Parser[ModuleContent.Main] {
    val parser: P[ModuleContent.Main] =
      for (version ← "main" ~ WS ~ PathParser ~ WS.? ~ ";")
        yield new ModuleContent.Main(version)
  }

  object PackageParser extends Parser[ModuleContent.Package] {
    val parser: P[ModuleContent.Package] =
      for (version ← "package" ~ WS ~ PathParser ~ WS.? ~ ";")
        yield new ModuleContent.Package(version)
  }

  object RequireParser extends Parser[ModuleContent.Require] {
    val parser: P[ModuleContent.Require] =
      for ((flags, name, version) ← ModuleRequireFlagParser ~ "requires" ~ WS ~ PathParser ~ WS.? ~ (":" ~ WS.? ~ StringLiteral ~ WS.?).? ~ ";")
        yield new ModuleContent.Require(new Module.Require(name, flags, Optional.ofNullable(version.orNull)))
  }

  object ExportParser extends Parser[ModuleContent.Export] {
    val parser: P[ModuleContent.Export] =
      for ((flags, name, modules) ← ModuleExportFlagParser ~ "exports" ~ WS.? ~ PathParser ~ WS.? ~
        ("to" ~ WS.? ~ PathParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?) ~ WS.?).? ~ ";")
        yield new ModuleContent.Export(new Module.Export(name, flags, modules.getOrElse(Nil).asJava))
  }

  object OpensParser extends Parser[ModuleContent.Opens] {
    val parser: P[ModuleContent.Opens] =
      for ((flags, name, modules) ← ModuleOpensFlagParser ~ "opens" ~ WS.? ~ PathParser ~ WS.? ~
        ("to" ~ WS.? ~ PathParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?) ~ WS.?).? ~ ";")
        yield new ModuleContent.Opens(new Module.Open(name, flags, modules.getOrElse(Nil).asJava))
  }

  object UsesParser extends Parser[ModuleContent.Use] {
    val parser: P[ModuleContent.Use] =
      for (path ← "uses" ~ WS.? ~ PathParser ~ WS.? ~ ";")
        yield new ModuleContent.Use(path)
  }

  object ProvidesParser extends Parser[ModuleContent.Provides] {
    val parser: P[ModuleContent.Provides] =
      for ((service, providers) ← "provides" ~ WS.? ~ PathParser ~ WS.? ~
        ("with" ~ WS.? ~ PathParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?) ~ WS.?).? ~ ";")
        yield new ModuleContent.Provides(new Module.Provide(service, providers.getOrElse(Nil).asJava))
  }

  val parser: P[ModuleContent] = P {
    VersionParser |
      MainParser |
      PackageParser |
      UsesParser |
      ProvidesParser |
      RequireParser |
      ExportParser |
      OpensParser
  }
}