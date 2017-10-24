val getCommitSha = taskKey[String]("Returns the current git commit SHA")

getCommitSha := {
  import scala.sys.process._
  "git rev-parse HEAD".lineStream_!.head
}

val getCurrentDate = taskKey[String]("Get current date")

getCurrentDate := {
  new java.text.SimpleDateFormat("yyyy-HH-mm'T'hh:MM:ss.SSSSXX").format(new java.util.Date())
}

val getBuildInfo = taskKey[String]("Get information about the build")

getBuildInfo := {
  s"""Map(
     |  "name" -> "${name.value}",
     |  "organization" -> "${organization.value}",
     |  "version" -> "${version.value}",
     |  "date" -> "${getCurrentDate.value}",
     |  "commit" -> "${getCommitSha.value}",
     |  "scalaVersion" -> "${scalaVersion.value}",
     |  "libraryDependencies" -> "${libraryDependencies.value}"
     |)
   """.stripMargin
}

val makeBuildInfo = taskKey[Seq[File]]("Makes the BuildInfo.scala file")

makeBuildInfo := {
  val resourceDir: File = (sourceManaged in Compile).value
  val configFile: File = new File(resourceDir, "BuildInfo.scala")
  val content =
    s"""
       |package build
       |
       |object BuildInfo {
       |  val info: Map[String, String] = ${getBuildInfo.value}
       |}
     """.stripMargin
  IO.write(configFile, content)
  Seq(configFile)
}

sourceGenerators in Compile += makeBuildInfo.taskValue

import sbt.complete.DefaultParsers._

val hello = inputKey[String]("Hello World")

hello := {
  val name: String = (Space ~> StringBasic).parsed
  val greeting = s"Hello $name"
  streams.value.log.info(greeting)
  greeting
}

val useHello = taskKey[String]("Using hello")

useHello := {
  val result = hello.toTask(" Dennis").value
  val msg = s"useHello: '$result'"
  streams.value.log(msg)
  msg
}