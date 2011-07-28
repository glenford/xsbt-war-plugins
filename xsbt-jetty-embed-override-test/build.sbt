seq(webSettings:_*)

name := "xsbt-jetty-embed-override-test"

version := "0.1"

jettyEmbeddedStartup := Some("net.usersource.jettyembed.jetty6.MyStartup")

libraryDependencies ++= Seq(
  "com.sun.jersey" % "jersey-server" % "1.3",
  "com.sun.jersey" % "jersey-core" % "1.3",
  "org.mortbay.jetty" % "jetty" % "6.1.21",
  "org.mortbay.jetty" % "jetty-sslengine" % "6.1.21"
)
