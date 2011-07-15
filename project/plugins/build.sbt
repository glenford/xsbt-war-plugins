
resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

libraryDependencies += "com.github.mpeltonen" %% "sbt-idea" % "0.10.0-SNAPSHOT"

resolvers += Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)
//resolvers += Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-snapshots/"))(Resolver.defaultIvyPatterns)

libraryDependencies <+= sbtVersion("org.scala-tools.sbt" %% "scripted-plugin" % _)

