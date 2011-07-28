seq(webSettings:_*)

seq(jettyEmbedSettings:_*)

name := "xsbt-jetty-embed-test"

version := "0.1"

//jettyVersion := "7.3.0.v20110203"

libraryDependencies ++= Seq(
  "com.sun.jersey" % "jersey-server" % "1.3",
  "com.sun.jersey" % "jersey-core" % "1.3"
)


