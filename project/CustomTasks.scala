import sbt._
import scala.reflect.runtime.universe._

object CustomTasks extends AutoPlugin with CompileAndRun with CustomLoader {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    lazy val compileFile = inputKey[Unit]("compile a single file, needs two inputs the input file and output dir relative to target dir")
    lazy val runFile = inputKey[Unit]("Run a single file, needs fqcn")
    lazy val createTypeTagST = inputKey[TypeTag[_]]("Create a TypeTag for a simple type")
    lazy val createTypeTagHKT = inputKey[TypeTag[_]]("Create a TypeTag for a higher kinded type")
    lazy val getFullClassLoader = taskKey[ClassLoader]("Returns a classloader that can load all project dependencies and compiled sources")
  }

  import autoImport._

  lazy val defaultSettings: Seq[Setting[_]] = Seq(
    compileFile := {
      import sbt.Keys._
      val compilers = Keys.compilers.value
      val classpath: Seq[File] = (fullClasspath in Compile).value.map(_.data)
      val options: Seq[String] = (scalacOptions in Compile).value
      val inputs: xsbti.compile.Inputs = (compileInputs in Compile in compile).value
      val cache: xsbti.compile.GlobalsCache = inputs.setup().cache()
      val log = streams.value.log

      val userInput: Seq[String] = Def.spaceDelimited("Relative input and output dir in target dir").parsed
      val targetDir: File = target.value
      val inputFile: File = userInput.head.split("/").foldLeft(targetDir)((c, e) => c / e)
      val outputDir: File = userInput.drop(1).head.split("/").foldLeft(targetDir)((c, e) => c / e)

      compilers.scalac() match {
        case compiler: sbt.internal.inc.AnalyzingCompiler =>
          compileSingleFile(
            compiler,
            inputFile,
            classpath,
            outputDir,
            options,
            cache,
            log
          )
        case _ => sys.error("Expected a 'sbt.internal.inc.AnalyzingCompiler' compiler")
      }

    },

    runFile := {
      import sbt.Keys._
      implicit val runnerToUse: ScalaRun = runner.value
      val userInput: Seq[String] = Def.spaceDelimited("fqcn of class to run").parsed
      val options: Seq[String] = (scalacOptions in Compile).value
      val fullClasspath: Seq[File] = (Keys.fullClasspath in Compile).value.map(_.data)
      val classDirectory: File = (Keys.classDirectory in Compile).value
      val targetDir: File = Keys.target.value
      val classpath = Seq(classDirectory, targetDir) ++ fullClasspath
      val log = streams.value.log
      runSingleFile(userInput.head, classpath, options, log)
    },

    createTypeTagST := {
      val fqcn: Seq[String] = Def.spaceDelimited("fqcn of SimpleType like 'scala.Int'").parsed
      val cl = getFullClassLoader.value
      createTypeTagForST(fqcn.head, Option(cl))
    },

    createTypeTagHKT := {
      val fqcn = Def.spaceDelimited("fqcn of Higherkinded type, needs two types: 'scala.collection.immutable.List' and 'scala.Int'").parsed
      val cl = getFullClassLoader.value
      createTypeTagForHKT(fqcn.head, fqcn.drop(1).head, Option(cl))
    },

    getFullClassLoader := {
      val scalaInstance = Keys.scalaInstance.value
      val fullClasspath: Seq[File] = (Keys.fullClasspath in Compile).value.map(_.data)
      val classDirectory: File = (Keys.classDirectory in Compile).value
      val targetDir: File = Keys.target.value
      val classpath = Seq(classDirectory, targetDir) ++ fullClasspath
      val cl: ClassLoader = sbt.internal.inc.classpath.ClasspathUtilities.makeLoader(classpath, scalaInstance)
      cl
    },
  )

  override def projectSettings: Seq[Def.Setting[_]] = {
    defaultSettings
  }
}

trait CompileAndRun {

  import sbt._

  final val MaxErrors = 1000

  final val NoChanges = new xsbti.compile.DependencyChanges {
    def isEmpty = true
    def modifiedBinaries = Array()
    def modifiedClasses = Array()
  }

  final val NoopCallback = new xsbti.AnalysisCallback {
    override def startSource(source: File): Unit = {}
    override def mainClass(sourceFile: File, className: String): Unit = {}
    override def apiPhaseCompleted(): Unit = {}
    override def enabled(): Boolean = false
    override def binaryDependency(onBinaryEntry: File, onBinaryClassName: String, fromClassName: String, fromSourceFile: File, context: xsbti.api.DependencyContext): Unit = {}
    override def generatedNonLocalClass(source: File, classFile: File, binaryClassName: String, srcClassName: String): Unit = {}
    override def problem(what: String, pos: xsbti.Position, msg: String, severity: xsbti.Severity, reported: Boolean): Unit = {}
    override def dependencyPhaseCompleted(): Unit = {}
    override def classDependency(onClassName: String, sourceClassName: String, context: xsbti.api.DependencyContext): Unit = {}
    override def generatedLocalClass(source: File, classFile: File): Unit = {}
    override def api(sourceFile: File, classApi: xsbti.api.ClassLike): Unit = {}
    override def usedName(className: String, name: String, useScopes: java.util.EnumSet[xsbti.UseScope]): Unit = {}
  }

  def compileSingleFile(
                         compiler: sbt.internal.inc.AnalyzingCompiler,
                         fileToCompile: File,
                         classpath: Seq[File],
                         outputDir: File,
                         options: Seq[String],
                         cache: xsbti.compile.GlobalsCache,
                         log: sbt.internal.util.ManagedLogger): Unit = {

    log.info(s"Compiling a single file: $fileToCompile")

    compiler.apply(
      Array(fileToCompile),
      NoChanges,
      classpath.toArray,
      outputDir,
      options.toArray,
      NoopCallback,
      MaxErrors,
      cache,
      log
    )
  }

  def runSingleFile(fqcn: String,
                    classpath: Seq[File],
                    options: Seq[String],
                    log: Logger
                   )(implicit scalaRun: ScalaRun): scala.util.Try[Unit] = {
    log.info(s"Running: single file: $fqcn")
    Run.run(fqcn, classpath, options, log).map { _ =>
      log.info(s"Successfully executed: $fqcn")
    } recover { case t: Throwable =>
      log.error(s"Failure running: $fqcn, reason: ${t.getMessage}")
      throw t
    }
  }
}
