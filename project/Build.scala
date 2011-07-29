
import sbt._
import sbt.Keys._

object BuildSettings {
  val buildOrganization = "net.usersource"
  val buildVersion      = "0.3"
  val buildScalaVersion = "2.8.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion
  )
}

object Resolvers {
  val apacheTomcatRepo = "Tomcat Repo" at "http://tomcat.apache.org/dev/dist/m2-repository"
  val webPlugin = "Web plugin repo" at "http://siasia.github.com/maven2"
}

object Dependencies {

  val sbtVersion      = "0.10.1"
  val sbtIo           = "org.scala-tools.sbt" %% "io" % sbtVersion
  val sbtLogging      = "org.scala-tools.sbt" %% "logging" % sbtVersion
  val sbtClasspath    = "org.scala-tools.sbt" %% "classpath" % sbtVersion
  val sbtProcess      = "org.scala-tools.sbt" %% "process" % sbtVersion

  val jetty6Version = "6.1.22"
  val jetty6 = "org.mortbay.jetty" % "jetty" % jetty6Version % "optional"
  val jetty6SSL = "org.mortbay.jetty" % "jetty-sslengine" % jetty6Version % "optional"

  val jetty7Version = "7.3.0.v20110203"
  val jetty7 = "org.eclipse.jetty" % "jetty-webapp" % jetty7Version % "optional"

  val jetty8Version = "8.0.0.M3"
  val jetty8 = "org.eclipse.jetty" % "jetty-webapp" % jetty8Version % "optional"

  val tomcat7Version = "7.0.2"
  val tomcat7 = "org.apache.tomcat" % "tomcat-catalina" % tomcat7Version % "optional"
  val tomcat7embed = "org.apache.tomcat.embed" % "tomcat-embed-core" % tomcat7Version % "optional"

  val jersyVersion = "1.3"
  val jerseyServer    = "com.sun.jersey" % "jersey-server" % jersyVersion % "optional"
  val jerseyCore      = "com.sun.jersey" % "jersey-core" % jersyVersion % "optional"
}

object XsbtWarPluginsBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  val commonDependencies = Seq(
    sbtIo,
    sbtLogging,
    sbtClasspath,
    sbtProcess
  )

  val jetty6embedDependencies = Seq(
    jetty6,
    jetty6SSL
  )

  val jetty7embedDependencies = Seq(
    jetty7
  )

  val jetty8embedDependencies = Seq(
    jetty8
  )

  val tomcat7embedDependencies = Seq(
    tomcat7,
    tomcat7embed
  )

  val jerseyDependencies = Seq(
    jerseyServer,
    jerseyCore
  )

 
  private def copyClassFiles(base: File):Seq[(File,String)] = {
    val jetty6class = "jetty6-startup/target/scala-2.8.1.final/classes/net/usersource/jettyembed/jetty6/Startup.class"
    val jetty6 = (new File(jetty6class), "startup/net/usersource/jettyembed/jetty6/Startup.class.precompiled")

    val jetty7class =  "jetty7-startup/target/scala-2.8.1.final/classes/net/usersource/jettyembed/jetty7/Startup.class"
    val jetty7 = (new File(jetty7class), "startup/net/usersource/jettyembed/jetty7/Startup.class.precompiled")

    val tomcat7class = "tomcat7-startup/target/scala-2.8.1.final/classes/net/usersource/tomcatembed/tomcat7/Startup.class"
    val tomcat7 = (new File(tomcat7class), "startup/net/usersource/tomcatembed/tomcat7/Startup.class.precompiled")

    Seq(jetty6,jetty7,tomcat7)
  }

  lazy val jettyEmbed = Project("jetty-embed", file("xsbt-jetty-embed"), 
    settings = buildSettings ++ Seq(
      name := "xsbt-jetty-embed",
      sbtPlugin := true,
      resolvers += webPlugin,
      libraryDependencies += "com.github.siasia" %% "xsbt-web-plugin" % "0.1.1-0.10.1",
      libraryDependencies ++= commonDependencies,
      (mappings in packageBin in Compile) <++= ((resourceManaged in Compile) map { dir => copyClassFiles( dir ) }) map { x => x }
    )
  ) dependsOn(jetty6Startup,jetty7Startup,tomcat7Startup)

  lazy val jetty6Startup = Project("jetty6-startup", file("jetty6-startup"),
    settings = buildSettings ++ Seq(
      name := "jetty6-startup",
      libraryDependencies ++= jetty6embedDependencies
    )
  )
  
  lazy val jetty7Startup = Project("jetty7-startup", file("jetty7-startup"),
    settings = buildSettings ++ Seq(
      name := "jetty7-startup",
      libraryDependencies ++= jetty7embedDependencies
    )
  )

  lazy val tomcat7Startup = Project("tomcat7-startup", file("tomcat7-startup"),
    settings = buildSettings ++ Seq(
      name := "tomcat7-startup",
      libraryDependencies ++= tomcat7embedDependencies,
      resolvers += apacheTomcatRepo
    )
  )

  override def projects = Seq(jettyEmbed, jetty6Startup, jetty7Startup, tomcat7Startup)

}
