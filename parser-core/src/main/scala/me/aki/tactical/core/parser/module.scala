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
  object VersionParser extends Parser[String] {
    val parser: P[String] =
      for (version ← "version" ~ WS ~ StringLiteral ~ WS.? ~ ";") yield version
  }

  object MainParser extends Parser[Path] {
    val parser: P[Path] =
      for (main ← "main" ~ WS ~ PathParser ~ WS.? ~ ";") yield main
  }

  object PackageParser extends Parser[Path] {
    val parser: P[Path] =
      for (pkg ← "package" ~ WS ~ PathParser ~ WS.? ~ ";") yield pkg
  }

  object RequireParser extends Parser[Module.Require] {
    val parser: P[Module.Require] =
      for ((flags, name, version) ← ModuleRequireFlagParser ~ "requires" ~ WS ~ PathParser ~ WS.? ~ (":" ~ WS.? ~ StringLiteral ~ WS.?).? ~ ";")
        yield new Module.Require(name, flags, Optional.ofNullable(version.orNull))
  }

  object ExportParser extends Parser[Module.Export] {
    val parser: P[Module.Export] =
      for ((flags, name, modules) ← ModuleExportFlagParser ~ "exports" ~ WS.? ~ PathParser ~ WS.? ~
        ("to" ~ WS.? ~ PathParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?) ~ WS.?).? ~ ";")
        yield new Module.Export(name, flags, modules.getOrElse(Nil).asJava)
  }

  object OpensParser extends Parser[Module.Open] {
    val parser: P[Module.Open] =
      for ((flags, name, modules) ← ModuleOpensFlagParser ~ "opens" ~ WS.? ~ PathParser ~ WS.? ~
        ("to" ~ WS.? ~ PathParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?) ~ WS.?).? ~ ";")
        yield new Module.Open(name, flags, modules.getOrElse(Nil).asJava)
  }

  object UsesParser extends Parser[Path] {
    val parser: P[Path] =
      for (path ← "uses" ~ WS.? ~ PathParser ~ WS.? ~ ";") yield path
  }

  object ProvidesParser extends Parser[Module.Provide] {
    val parser: P[Module.Provide] =
      for ((service, providers) ← "provides" ~ WS.? ~ PathParser ~ WS.? ~
        ("with" ~ WS.? ~ PathParser.rep(min = 1, sep = WS.? ~ "," ~ WS.?) ~ WS.?).? ~ ";")
        yield new Module.Provide(service, providers.getOrElse(Nil).asJava)
  }

  val parser: P[ModuleContent] = P {
    P { for (version ← VersionParser) yield new ModuleContent.Version(version) } |
      P { for (main ← MainParser) yield new ModuleContent.Main(main) } |
      P { for (pkg ← PackageParser) yield new ModuleContent.Package(pkg) } |
      P { for (use ← UsesParser) yield new ModuleContent.Use(use) } |
      P { for (provide ← ProvidesParser) yield new ModuleContent.Provides(provide) } |
      P { for (require ← RequireParser) yield new ModuleContent.Require(require) } |
      P { for (export ← ExportParser) yield new ModuleContent.Export(export) } |
      P { for (open ← OpensParser) yield new ModuleContent.Opens(open) }
  }
}