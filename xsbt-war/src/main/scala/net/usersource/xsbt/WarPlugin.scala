
package net.usersource.xsbt

import sbt._
import sbt.classpath.ClasspathUtilities
import sbt.Keys._
import sbt.Defaults._
import sbt.Project.Initialize

//
// This plugin is Based upon the great work of siasia => https://github.com/siasia/xsbt-web-plugin.git
// I hope to refactor this at some point to be based on that plugin rather than duplicate its functions
//

object WarPlugin extends Plugin {

  override lazy val settings = warSettings ++ super.settings

  val packageWar = TaskKey[File]("package-war")
  val prepareWar = TaskKey[Seq[(File, String)]]("prepare-war")

  val temporaryWarPath = SettingKey[File]("temporary-war-path")
  val warResources = SettingKey[PathFinder]("war-resources")
  val warUnmanaged = SettingKey[PathFinder]("war-unmanaged")

  val warExtra = TaskKey[PathFinder]("war-extra")
  val warClassPath = TaskKey[PathFinder]("war-classpath")

  private def copyFlat(sources: Iterable[File], destinationDirectory: File): Set[File] = {
    val map = sources.map(source => (source.asFile, destinationDirectory / source.getName))
    IO.copy(map)
  }

  def packageWarTask: Initialize[Task[Seq[(File, String)]]] = prepareWar map { (pw) => pw }

  def prepareWarTask(warContents: PathFinder, warPath: File, classpath: PathFinder, extraClassPath: PathFinder, ignore: PathFinder, defaultExcludes: FileFilter, slog: Logger): Seq[(File, String)] = {

    val log = slog.asInstanceOf[AbstractLogger]

    log.debug("Preparing for war")

    val webInfPath = warPath / "WEB-INF"
    log.debug("Output WEB-INF path [" + webInfPath + "]")

    val webLibDirectory = webInfPath / "lib"
    val classesTargetDirectory = webInfPath / "classes"

    val combinedClassPath = classpath +++ extraClassPath
    val (libs, directories) = combinedClassPath.get.toList.partition(ClasspathUtilities.isArchive)

    val wcToCopy = for {
                        dir <- warContents.get
                        file <- dir.descendentsExcept("*", defaultExcludes).get
                        val target = Path.rebase(dir, warPath)(file).get
                   } yield (file, target)

  
    val classesAndResources = for {
                                   dir <- directories
                                   file <- dir.descendentsExcept("*", defaultExcludes).get
                                   val target = Path.rebase(dir, classesTargetDirectory)(file).get
                   } yield (file, target)

    if(log.atLevel(Level.Debug)) {
      directories.foreach(d => log.debug(" Copying the contents of directory " + d + " to " + classesTargetDirectory))
      libs.foreach(lib => log.debug(" Coping library [" + lib + "] to [" + webLibDirectory + "]"))
    }

    val copiedWebapp = IO.copy(wcToCopy)
    val copiedClasses = IO.copy(classesAndResources)
    val copiedLibs = copyFlat(libs, webLibDirectory)
    val toRemove = scala.collection.mutable.HashSet(((warPath ** "*") --- ignore).get.toSeq : _*)
    toRemove --= copiedWebapp
    toRemove --= copiedClasses
    toRemove --= copiedLibs

    val (dirs, files) = toRemove.toList.partition(_.isDirectory)

    if(log.atLevel(Level.Debug))
      files.foreach(r => log.debug("Pruning file " + r))

    IO.delete(files)
    IO.deleteIfEmpty(dirs.toSet)
    ((warPath).descendentsExcept("*", defaultExcludes) --- ignore) x (relativeTo(warPath)|flat)
  }

  val warSettings: Seq[Project.Setting[_]] = {
    Seq(
      temporaryWarPath <<= (target){ (target) => target / "webapp" },
      warResources <<= (sourceDirectory in Runtime, defaultExcludes) { (sd, defaultExcludes) => sd / "webapp" },
      warUnmanaged := PathFinder.empty,
      warExtra := PathFinder.empty,
      warClassPath <<= (fullClasspath in Runtime) map { (fcp) => fcp.map(_.data) },
      prepareWar <<= (copyResources in Runtime, warResources, temporaryWarPath, warClassPath, warExtra, warUnmanaged, defaultExcludes, streams) map {
                        (r, w, wp, cp, excp, wu, excludes, s) => prepareWarTask(w, wp, cp, excp, wu, excludes, s.log)
      },
      configuration in packageWar := Compile,
      artifact in packageWar <<= name(n => Artifact(n, "war", "war"))
    ) ++ packageTasks(packageWar, packageWarTask)
  }

}

