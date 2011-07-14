import collection.Seq
import sbt._, Keys._

object BuildSettings {
  val buildOrganization = "net.usersource"
  val buildVersion      = "0.2"
  val buildScalaVersion = "2.8.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion
  )
}

object Resolvers {
  val javaDotNetRepo = "Glassfish Repo" at "http://download.java.net/maven/glassfish/"
}

object Dependencies {
  val sbtVersion      = "0.10.0"
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

  val jersyVersion = "1.3"
  val jerseyServer    = "com.sun.jersey" % "jersey-server" % jersyVersion % "optional"
  val jerseyCore      = "com.sun.jersey" % "jersey-core" % jersyVersion % "optional"
}

object XsbtWarPluginsBuild extends Build {
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

  val jerseyDependencies = Seq(
    jerseyServer,
    jerseyCore
  )

  lazy val root = Project("xsbt-war-plugins", file(".") ) aggregate( war, warExample, jettyEmbed ) 
    
  lazy val war = Project("xsbt-war", file("xsbt-war"),
    settings = buildSettings ++ Seq(
      name := "xsbt-war-plugin",
      sbtPlugin := true,
      libraryDependencies ++= commonDependencies
    )
  )

  lazy val warExample = Project("xsbt-war-example", file("xsbt-war-example"),
    settings = buildSettings ++ Seq(
      name := "xsbt-war-example",
      libraryDependencies ++= jerseyDependencies
    )
  ) dependsOn(war)

  private def copyClassFiles(base: File):Seq[(File,String)] = {
    val jetty6class = "jetty6-startup/target/scala-2.8.1.final/classes/net/usersource/jettyembed/jetty6/Startup.class"
    val jetty6 = (new File(jetty6class), "startup/net/usersource/jettyembed/jetty6/Startup.class.precompiled")

    val jetty7class =  "jetty7-startup/target/scala-2.8.1.final/classes/net/usersource/jettyembed/jetty7/Startup.class"
    val jetty7 = (new File(jetty7class), "startup/net/usersource/jettyembed/jetty7/Startup.class.precompiled")
   
    Seq(jetty6,jetty7)
  }

  lazy val jettyEmbed = Project("jetty-embed", file("xsbt-jetty-embed"), 
    settings = buildSettings ++ Seq(
      name := "xsbt-jetty-embed",
      sbtPlugin := true,
      libraryDependencies ++= commonDependencies,
      (mappings in packageBin in Compile) <++= ((resourceManaged in Compile) map { dir => copyClassFiles( dir ) }) map { x => x }
    )
  ) dependsOn(war,jetty6Startup,jetty7Startup)

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

  override def projects = Seq(root, war, warExample, jetty6Startup, jetty7Startup, jettyEmbed)
}
