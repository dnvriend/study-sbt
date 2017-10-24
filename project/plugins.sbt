resolvers += Resolver.sonatypeRepo("public")

// https://github.com/sbt/sbt-scalariform
// to format scala source code
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.1")

// enable updating file headers eg. for copyright
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "3.0.2")

// a rewrite and linting tool
// see: https://scalacenter.github.io/scalafix/
// see: http://scalameta.org/tutorial/#SemanticDB
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.5.3")