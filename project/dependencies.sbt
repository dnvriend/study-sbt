libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.7"
libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.0"
libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.8.0"
libraryDependencies += "io.circe" %% "circe-yaml" % "0.6.1"

dependsOn(RootProject(uri("git://github.com/dnvriend/dnvriend-ops.git")))