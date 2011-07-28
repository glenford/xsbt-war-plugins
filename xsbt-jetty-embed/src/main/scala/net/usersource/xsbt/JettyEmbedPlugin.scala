
package net.usersource.xsbt

import sbt._
import sbt.Keys._
import sbt.Defaults._
import sbt.Project.Initialize


import sbt.File

trait EmbedPlugin extends Plugin {
  
  // these are set by the xsbt-web plugin (https://github.com/siasia/xsbt-web-plugin)
  val prepareWar = TaskKey[Seq[(File, String)]]("prepare-webapp")
  val temporaryWarPath = SettingKey[File]("temporary-war-path")
  val packageWar = TaskKey[File]("package-war")

  def prepareStartupClass( classDir: File, className: String, log :Logger ): Option[(File,String)] = {
    val fileName = className.replace('.','/') + ".class"
    val file = new File(classDir, fileName)
    if(file.exists()) {
      log.debug("Found startup class [" + file.getAbsolutePath + "]")
      Some((file,fileName))
    }
    else {
      log.debug("Startup class not found, unpacking precompiled version")
      val pluginJar = new File("project/plugins/lib/xsbt-jetty-embed_2.8.1-0.3.jar")
      log.debug("Using plugin JAR [" + pluginJar.absolutePath + "]")
      val files = IO.unzip(pluginJar, classDir, filter = (s: String) => s.endsWith(fileName + ".precompiled") )
      log.debug("Unpacked [" + files.head.getAbsolutePath + "]" )
      IO.move(files.head ,classDir / fileName)
      if(file.exists()) Some((file,fileName))
      else None
    }
  }

  def handleNoStartup(startup: String, log: Logger) = {
    log.error("Unable to find the startup class file for [" + startup + "] please check your startup class setting")
    Seq.empty
  }

  def embedIntoWarPath( warPath: File, startUpFile: File, relPath: String, className: String, deps: Seq[File], log: Logger ) = {
    log.debug("Resolves to startup class file [" + startUpFile.absolutePath + "]")
    val dest = warPath / relPath
    IO.copyFile(startUpFile,dest,false)

    log.debug("Embedding dependencies [" + deps + "]")
    deps.foreach { (dep) => {
      log.debug("Unpacking [" + dep + "] to [" + warPath / dep.name + "]")
      val filter: NameFilter = (s: String) => !s.endsWith(".MF")
      IO.unzip(dep,warPath,filter)
    } }

    // TODO: need a better way to get descendents
    (warPath).descendentsExcept("*", ".svn") x (relativeTo(warPath)|flat)
  }

  def embedPrepare(classDir: File, startup: String, log: AbstractLogger, warPath: File, tomcatDeps: scala.Seq[File]): Seq[(File, String)] = {
    prepareStartupClass(classDir, startup, log) match {
      case Some((file: File, relPath: String)) => embedIntoWarPath(warPath, file, relPath, startup, tomcatDeps, log)
      case None => handleNoStartup(startup, log)
    }
  }
}


object TomcatEmbedPlugin extends EmbedPlugin {
  val TomcatEmbed = config("tomcatEmbed") extend(Runtime)
  val tomcat7DefaultStartupClass = "net.usersource.tomcatembed.tomcat7.Startup"
  val embedTomcatPrepare = TaskKey[Seq[(File,String)]]("embed-tomcat-prepare")
  val embedTomcat = TaskKey[File]("embed-tomcat", "embed tomcat to produce an executeable war")
  val tomcatVersion = SettingKey[String]("embed-tomcat-version")
  val tomcatEmbeddedStartup = TaskKey[Option[String]]("embed-tomcat-startup")
  val tomcatEmbeddedClasspath = TaskKey[Seq[ModuleID]]("embed-tomcat-classpath")

  def embedTomcatTask:  Initialize[Task[Seq[(File, String)]]] = embedTomcatPrepare map { (e) => e }



  def embedTomcatPrepareTask(warPath: File, startup: String, classDir: File, tomcatDeps: Seq[File], slog: Logger): Seq[(File, String)] = {
    val log = slog.asInstanceOf[AbstractLogger]
    log.debug("Preparing to embed tomcat into war currently in [" + warPath + "] with [" + startup + "]")
    embedPrepare(classDir, startup, log, warPath, tomcatDeps)
  }

  private def determineStartup = {
    tomcatVersion map {
      (version) => {
        version match {
          case v: String if(v.startsWith("7") || v.startsWith("8")) => Some(tomcat7DefaultStartupClass)
          case _ => None
        }
      }
    }
  }

  private def determineClasspath( version: String ): Seq[ModuleID] = {
    version match {
      case v: String if(v.startsWith("7") || v.startsWith("8")) => {
        val tomcat7 = "org.apache.tomcat" % "tomcat-catalina" % v % "tomcatEmbed"
        val tomcat7embed = "org.apache.tomcat.embed" % "tomcat-embed-core" % v % "tomcatEmbed"
        Seq(tomcat7,tomcat7embed)
      }
    }
  }

  lazy val tomcatEmbedSettings: Seq[Project.Setting[_]] = {
    Seq(
      tomcatVersion := "7.0.2",
      tomcatEmbeddedStartup <<= determineStartup,
      configuration := TomcatEmbed,
      ivyConfigurations += config("tomcatEmbed"),
      embedTomcatPrepare <<= (temporaryWarPath, tomcatEmbeddedStartup, classDirectory in Compile, update, streams) map {
                        (path,start, cd, report, s) => {
                          val deps = report.matching(configurationFilter(name = "tomcatEmbed") && artifactFilter(`type` = "jar"))
                          embedTomcatPrepareTask(path, start.get, cd, deps, s.log)
                        }
      },
      embedTomcatPrepare <<= embedTomcatPrepare dependsOn(prepareWar),
      packageOptions in embedTomcat <<= tomcatEmbeddedStartup map { main: Option[String] => Seq(Package.MainClass(main.get)) },
      artifact in embedTomcat <<= name(n => Artifact(n, "war", "war")),
      libraryDependencies <++= tomcatVersion.apply(determineClasspath(_))
    ) ++ packageTasks(embedTomcat,embedTomcatTask)
  }

  //override lazy val settings = super.settings ++ tomcatEmbedSettings
}


object JettyEmbedPlugin extends EmbedPlugin {

  val JettyEmbed = config("jettyEmbed") extend(Runtime)
  val jetty6DefaultStartupClass = "net.usersource.jettyembed.jetty6.Startup"
  val jetty7DefaultStartupClass = "net.usersource.jettyembed.jetty7.Startup"
  val embedJettyPrepare = TaskKey[Seq[(File,String)]]("embed-jetty-prepare")
  val embedJetty = TaskKey[File]("embed-jetty", "embed jetty to produce an executeable war")
  val jettyVersion = SettingKey[String]("embed-jetty-version")
  val jettyEmbeddedStartup = TaskKey[Option[String]]("embed-jetty-startup")
  val jettyEmbeddedClasspath = TaskKey[Seq[ModuleID]]("embed-jetty-classpath")


  def embedJettyTask:  Initialize[Task[Seq[(File, String)]]] = embedJettyPrepare map { (e) => e }

  def embedJettyPrepareTask(warPath: File, startup: String, classDir: File, jettyDeps: Seq[File], slog: Logger): Seq[(File, String)] = {
    val log = slog.asInstanceOf[AbstractLogger]
    log.debug("Preparing to embed jetty into war currently in [" + warPath + "] with [" + startup + "]")
    embedPrepare(classDir, startup, log, warPath, jettyDeps)
  }

  private def determineStartup = {
    jettyVersion map {
      (version) => {
        version match {
          case v: String if v.startsWith("6") => Some(jetty6DefaultStartupClass)
          case v: String if(v.startsWith("7") || v.startsWith("8")) => Some(jetty7DefaultStartupClass)
          case _ => None
        }
      }
    }
  }

  private def determineClasspath( version: String ): Seq[ModuleID] = {
    version match {
      case v: String if v.startsWith("6") => {
        val jetty6EmbedDependencies = "org.mortbay.jetty" % "jetty" % v % "jettyEmbed"
        val jetty6EmbedSSLDependencies = "org.mortbay.jetty" % "jetty-sslengine" % v % "jettyEmbed"
        Seq(jetty6EmbedDependencies, jetty6EmbedSSLDependencies)
      }
      case v: String if(v.startsWith("7") || v.startsWith("8")) => {
        val jetty7EmbedDependencies = "org.eclipse.jetty" % "jetty-webapp" % v % "jettyEmbed"
        Seq(jetty7EmbedDependencies)
      }
    }
  }

  lazy val jettyEmbedSettings: Seq[Project.Setting[_]] = {
    Seq(
      jettyVersion := "6.1.21",
      jettyEmbeddedStartup <<= determineStartup,
      configuration := JettyEmbed,
      ivyConfigurations += config("jettyEmbed"),
      embedJettyPrepare <<= (temporaryWarPath, jettyEmbeddedStartup, classDirectory in Compile, update, streams) map {
                        (path,start, cd, report, s) => {
                          val deps = report.matching(configurationFilter(name = "jettyEmbed") && artifactFilter(`type` = "jar"))
                          embedJettyPrepareTask(path, start.get, cd, deps, s.log)
                        }
      },
      embedJettyPrepare <<= embedJettyPrepare dependsOn(prepareWar),
      packageOptions in embedJetty <<= jettyEmbeddedStartup map { main: Option[String] => Seq(Package.MainClass(main.get)) },
      artifact in embedJetty <<= name(n => Artifact(n, "war", "war")),
      libraryDependencies <++= jettyVersion.apply(determineClasspath(_))
    ) ++ packageTasks(embedJetty,embedJettyTask)
  }

  //override lazy val settings = super.settings ++ jettyEmbedSettings
}

