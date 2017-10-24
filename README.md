# study-sbt
A small study on [sbt (the Scala Build Tool)](http://www.scala-sbt.org/).

## Introduction
The [Scala Build Tool](http://www.scala-sbt.org/) or sbt for short, is a build tool for building your source code. It is a very advanced tool and basically is just a workflow engine. In contrast to other build tools, scala is very simple, well, if you know just a couple of concepts.

## Build, Projects, Settings, Tasks, Keys, Configurations and Scopes
Lets introduce the core concepts of our build. When we create a project for example with the following command:

```bash
sbt new dnvriend/scala-seed.g8
```

Sbt will create a directory structure like the following:

```bash
study-sbt
├── LICENSE
├── README.md
├── build.sbt
└── src
    ├── main
    │   └── scala
    │       └── com
    │           └── github
    │               └── dnvriend
    │                   └── HelloWorld.scala
    └── test
        └── scala
            └── com
                └── github
                    └── dnvriend
                        ├── PersonTest.scala
                        └── TestSpec.scala
```

We see the well known directory structure `src/main` that will contain both our Scala and Java source code to be build and a `build.sbt` file that describes our build. The `build.sbt` file is optional. When we do not have a `build.sbt` file, sbt will use default values to still build or source code. 

When we as developers do not state otherwise (in the build.sbt file for example), sbt assumes that there is only a single project to be build. That single project is called 'the default project' and sbt will assume that the base directory of that single project is the current directory so '{.}'. 

In the example above, and for 90% of all projects this will be the case. A directory structure like above, most likely with a build.sbt file definiting some settings like eg. a name and a version.

### Build
What is the Build. Well, the build is what Sbt knows about what it is it should build. When we do not specify otherwise, sbt will assume a single project in the current directory, but we as developers can define other projects that Sbt should build. So the build says something about one or more projects that Sbt should build.

### Project
A Build concerns itself with one or more projects, and a projects concerns itself with settings. For example, which libraries should be used to compile the project with, which Scala version to use and maybe which version the project is eg. 1.0.0-SNAPSHOT version etc.

### Settings
So a Build builds projects, Projects define themselves using settings, then what is a Setting. Well, a Setting is a Key -> Value pair. It is something like:

```scala
name := "my-project"
version := "1.0.0-SNAPSHOT"
libraryDependencies += "foo" %% "bar" %% "1.0.0"
```

So a setting is just a Key -> Value pair. What is unique about settings is that the key -> value pair will be initialized by sbt start-up, so when you start SBT. So the values are initialized only once. 

### Tasks
So a Build builds projects, Projects define themselves using settings, and settings are Key -> Value pairs that are initialized only once when Sbt launches. Then what are tasks? A Task is a Key -> Value pair that is evaluated on demand. A Task exists to be evaluated every time it is needed. Most of the time Tasks are used for doing side effects like the task 'clean' or the task 'compile'. 

Because a Task is a Key -> Value pair, just like a Setting (which is also a Key -> Value pair), you can 'call' a Task by just typing the name of the Key and Sbt will evaluate the Key to a value.

We can show the result of either a Setting or a Task using the sbt-console with the help of the 'show' command. For example, when we type 'show name', sbt will evaluate the Key 'name' and return the evaluated value. Of course, because 'name' is a Setting, the initialization has already been done when Sbt started, so it will return the value immediately:

```scala
> name
[info] study-sbt
```

We can also evaluate a Task. As I stated earlier, a Task is just a Key -> Value pair that will be evaluated on demand so it exist to be evaluated when we need it and most of the time we use a Task to do side effects like the Task 'clean':

```scala
> show clean
[info] ()
[success] Total time: 0 s, completed 17-feb-2017 9:06:18
```

The Task clean evaluates to the value 'Unit' which is returned, because it does side effects like deleting the contents of the './target' directory.

### Recap until now
So a Build contains one or more projects. A project defines itself using settings. A Setting is just a Key -> Value pair that is initialized only once and a Task is a Key -> Value pair that will be evaluated on demand.

### Configurations
I will assume that you already know a little how sbt works and are already working with it so you know that sbt supports testing. For unit testing you will need for example the [ScalaTest](http://www.scalatest.org/) and if you are creating reactive applications, the [akka-testkit](http://doc.akka.io/docs/akka/2.4/scala/testing.html) library as a dependency. Also, we have split the code that is for testing from our business code. The code bases have different paths, eg. the test code exists in './src/main/test' and this code base has a dependency with the test libraries that our business code doesn't have. 

Sbt uses Configurations to segment Settings so it knows which setting to use when a certain task is being executed. There are a lot of Configurations defined in Sbt and you can also define your own. We will look into those a little bit later. 

For example, the key [sourceDirectories](https://github.com/sbt/sbt/blob/1.0.x/main/src/main/scala/sbt/Keys.scala#L140) lists all directories that will be used by sbt to build the project. For example, when we type 'test' then the sourceDirectories for the test configuration will be used. Lets say we want to create our own test Task which we will call 'mytest':

```scala
// first define a task key
lazy val mytest = taskKey[Unit]("My test key to show how scoped settings work")

// then implement the task key
mytest := {
	val dirs = (sourceDirectories in Test).value
	println(dirs)
}
```

When we run 'mytest' then the output is:

```bash
> mytest
List(
 /Users/dennis/projects/study-sbt/src/test/scala-2.12, 
 /Users/dennis/projects/study-sbt/src/test/scala, 
 /Users/dennis/projects/study-sbt/src/test/java, 
 /Users/dennis/projects/study-sbt/target/scala-2.12/src_managed/test
)
[success] Total time: 0 s, completed 17-feb-2017 12:52:31
```

Lets say that we want to create our own compile Task which we will call 'mycompile' then it could be this:

```scala
lazy val mycompile = taskKey[Unit]("My compile key to show how scoped settings work")

mycompile := {
	val dirs = (sourceDirectories in Compile).value
	println(dirs)
}
```

```bash
> mycompile
List(
 /Users/dennis/projects/study-sbt/src/main/scala-2.12, 
 /Users/dennis/projects/study-sbt/src/main/scala, 
 /Users/dennis/projects/study-sbt/src/main/java, 
 /Users/dennis/projects/study-sbt/target/scala-2.12/src_managed/main
)
[success] Total time: 0 s, completed 17-feb-2017 12:56:24
```

So the key 'sourceDirectories' has a different value for different scopes and it depends on the implementation of the Task where it looks to get the value of a Key. In our examples we specifically look for a value for `(sourceDirectories in Test).value` to get the value and for `(sourceDirectories in Compile).value`. 

We can also query sbt for these values without creating a custom Task. For example, to get the value of the key 'sourceDirectories' in the Configuration 'Test' we type:

```bash
> test:sourceDirectories
[info] * /Users/dennis/projects/study-sbt/src/test/scala-2.12
[info] * /Users/dennis/projects/study-sbt/src/test/scala
[info] * /Users/dennis/projects/study-sbt/src/test/java
[info] * /Users/dennis/projects/study-sbt/target/scala-2.12/src_managed/test
```

And for the Configuration 'Compile' we type:

```bash
> compile:sourceDirectories
[info] * /Users/dennis/projects/study-sbt/src/main/scala-2.12
[info] * /Users/dennis/projects/study-sbt/src/main/scala
[info] * /Users/dennis/projects/study-sbt/src/main/java
[info] * /Users/dennis/projects/study-sbt/target/scala-2.12/src_managed/main
```

Of course, a setting does not have to exist in a certain Configuration like eg 'name':

```bash
> test:name
[error] No such setting/task
[error] test:name
[error]
```

The setting 'name' also doesn't exist in the configuration 'compile':

```bash
> compile:name
[error] No such setting/task
[error] compile:name
[error]
```

But when we type the following:

```bash
> name
[info] study-sbt
```

The setting exists. How come? You can define settings specific for a Configuration like 'Test' or 'Compile', but you can also define settings that apply for all configurations. The setting 'name' is one of those settings that is the same for all configurations. In this case that is reasonable, it wouldn't make much sense to set the 'name' of the project to be different for Configurations. 

In Sbt you can use the symbol '*' and that means 'all' so if we want to get the value of the key 'name' in all configurations that we can also type:

```bash
> *:name
[info] study-sbt
```

When we just type 'name' for example, then sbt will assume that we want the value of key '*:name' thats why it works.

Lets say that we want to do something strange like setting the name of the project to a different name for the Configuration 'Test' only and lets say that the value in that configuration will be 'study-sbt-in-test', then we would add the following to `build.sbt`:

```scala
name in Test := "study-sbt-in-test"
```

Alternatively, we can also type the following in an Sbt session in which case the setting will not be persistent but only for the duration of the Sbt console session:

```scala
set name in Test := "study-sbt-in-test"
[info] Defining test:name
[info] The new value will be used by test:packageBin::packageOptions, test:packageSrc::packageOptions
[info] Reapplying settings...
[info] Set current project to study-sbt (in build file:/Users/dennis/projects/study-sbt/)
```

We will now query the value for name for different scopes:

```bash
> name
[info] study-sbt
> *:name
[info] study-sbt
> test:name
[info] study-sbt-in-test
> compile:name
[error] No such setting/task
[error] compile:name
[error]             ^
>
```

### Configuration by Task
A configuration can also be scoped to a specific Task for example add the following to build.sbt:

```scala
lazy val mysetting = settingKey[String]("My setting")

mysetting := "mysetting for the current project, all configurations and all tasks"

mysetting in Test := "mysetting for the current project, for the Test configuration and all tasks"

mysetting in Test in MyTask := "mysetting for the current project, for the Test configuration for the task MyTask only"

lazy val MyTask = taskKey[Unit]("My task")

MyTask := {
    val str = (mysetting in Test in MyTask).value
    println(str)
}
```

```bash
> MyTask
mysetting for the current project, for the Test configuration for the task MyTask only
[success] Total time: 0 s, completed 17-feb-2017 13:15:28
```

The task MyTask will look specifically for a value for the setting 'mysetting' and it will look in the Configuration 'Test' and for the task 'MyTask'. We have specified this by typing `(mysetting in Test in MyTask).value` so that is very specific. 

Of course, when the setting cannot be found, sbt will look for fallback alternatives, so if you comment out the line:

```scala
mysetting in Test in MyTask := "mysetting for the current project, for the Test configuration for the task MyTask only"
```

Sbt will use the next fallback and so on:

```bash
> MyTask
mysetting for the current project, for the Test configuration and all tasks
[success] Total time: 0 s, completed 17-feb-2017 13:18:38
```

### Configuration by Task 'initialCommands in console'
As we have seen, configurations can be scoped by task. In sbt this type of configuration is used when launching the REPL
using the 'console' eg: 'sbt console'. We have to configure the 'initialCommands' settingKey which is of type 'String' and
set the scope to the taskKey 'console':

```scala
initialCommands in console :=
"""
import scalaz._
import Scalaz._
import com.github.dnvriend._
val xs = List(1, 2, 3, 4, 5)
"""
```

When we launch the REPL from sbt then the following expressions will be evaluated.

### Keys
To be able to configure anything in the build, from a Setting to a Task, Keys play an important role because a Key allow us to bind a value to a name. As we have seen, a Key is simply a name that can be created with the method 'settingKey' and 'taskKey', and then you can use the newly created key and bind that key to a specific value in a specific Configuration and Task. For example:

```scala
name := "study-sbt"

name in Test := "study-sbt-in-test"

name in Compile := "study-sbt-in-compile"

name in Compile in compile := "study-sbt-in-compile-for-the-task-compile"
```

We can query these settings:

```bash
> name
[info] study-sbt
> *:name
[info] study-sbt
> test:name
[info] study-sbt-in-test
> compile:name
[info] study-sbt-in-compile
> compile:compile::name
[info] study-sbt-in-compile-for-the-task-compile
```

The last syntax is new and must be read as: 

```
Give me the value for the key 'name' in the configuration 'Compile' for the task 'compile'.
```

### Custom Configurations
We can also create our own configurations. Lets start right away by defining a configuration called 'my-config' that 
will be used by the task 'MyOtherTask':

```scala
lazy val MyConfig = config("my-config")

lazy val myOtherSetting = settingKey[String]("My other setting")

myOtherSetting := "mysetting for the current project, all configurations and all tasks"

myOtherSetting in MyConfig := "mysetting for the current project, for the MyConfig configuration and all tasks"

myOtherSetting in MyConfig in MyOtherTask := "mysetting for the current project, for the MyConfig configuration for the task MyOtherTask only"

lazy val MyOtherTask = taskKey[Unit]("My other task")

MyOtherTask := {
    val str = (myOtherSetting in MyConfig in MyOtherTask).value
    println(str)
}
```

We can use our custom configuration like any other:

```bash
> myOtherSetting
[info] mysetting for the current project, all configurations and all tasks
> my-config:myOtherSetting
[info] mysetting for the current project, for the MyConfig configuration and all tasks
> my-config:MyOtherTask::myOtherSetting
[info] mysetting for the current project, for the MyConfig configuration for the task MyOtherTask only

> MyOtherTask
mysetting for the current project, for the MyConfig configuration for the task MyOtherTask only
[success] Total time: 0 s, completed 17-feb-2017 14:02:51
```

### Dependent Tasks
We can make tasks dependent on one another. Lets create two tasks:

- 'task1' will return the String "Hello",
- 'task2' will use the value of 'task1', so it is dependent on task1 and will therefor call 'task1' to get its result. You can see here that tasks, like settings, return a value but do that on demand and will be evaluated every time when called. The task 'task2' will return the String "Hello World!", well thats the idea at least! Lets see if it works as intended:

```scala
lazy val task1 = taskKey[String]("task 1")

lazy val task2 = taskKey[String]("task 2")

task1 := {
    println("Evaluating task1")
    "Hello"
}

task2 := {
  println("Evaluating task2")
  s"${task1.value} World!"
}
```

Lets try it out:

```bash
> show task1
Evaluating task1
[info] Hello
[success] Total time: 0 s, completed 17-feb-2017 14:05:38

> show task2
Evaluating task1
Evaluating task2
[info] Hello World!
[success] Total time: 0 s, completed 17-feb-2017 14:05:40
```

It works! See, sbt isn't that difficult!

### Tasks for a certain configuration
Like a Setting, a Task can also have a different value for a different configuration:

```scala
lazy val task1 = taskKey[String]("task 1")

lazy val task2 = taskKey[String]("task 2")

task1 := {
    println("Evaluating task1 for current project for all configurations")
    "Hello all config"
}

task1 in Test := {
    println("Evaluating task1 for current project for Test config")
    "Hello test config"
}

task1 in Compile := {
    println("Evaluating task1 for current project for Compile config")
    "Hello compile config"
}


task2 := {
  println("Evaluating task2 for current project for all configurations")
  val task1Value = (task1 in Test).value
  s"$task1Value World!"
}
```

Lets try it out:

```bash
> show task1
Evaluating task1 for current project for all configurations
[info] Hello all config
[success] Total time: 0 s, completed 18-feb-2017 12:52:08

> show compile:task1
Evaluating task1 for current project for Compile config
[info] Hello compile config
[success] Total time: 0 s, completed 18-feb-2017 12:52:13

> show test:task1
Evaluating task1 for current project for Test config
[info] Hello test config
[success] Total time: 0 s, completed 18-feb-2017 12:52:16

> show task2
Evaluating task1 for current project for Test config
Evaluating task2 for current project for all configurations
[info] Hello test config World!
[success] Total time: 0 s, completed 18-feb-2017 12:52:22
```

### Task Dependencies
In the examples we have created dependencies between two tasks, task1 and task2. In the examples, task2 would ask task1 for its value. In effect the dependency is created in the implementation of the task like so:

```scala
lazy val task1 = taskKey[String]("task 1")

lazy val task2 = taskKey[String]("task 2")

task1 := {
    println("Evaluating task1")
    "Hello"
}

task2 := {
  println("Evaluating task2")
  s"${task1.value} World!"
}
```

In the example above we need the evaluated value of task1 to do some computation of our own but what if we just have tasks that do some side effects and all return Unit. What if we need to create a sequence between them, how do we do that? 

For example, we have the following three tasks:

```scala
lazy val task1 = taskKey[Unit]("task 1")

lazy val task2 = taskKey[Unit]("task 2")

lazy val task3 = taskKey[Unit]("task 3")

task1 := println("Task 1")

task2 := println("Task 2")

task3 := println("Task 3")
```

Lets try them out:

```bash
task1> task1
Task 1
[success] Total time: 0 s, completed 18-feb-2017 13:24:22
> task2
Task 2
[success] Total time: 0 s, completed 18-feb-2017 13:24:23
> task3
Task 3
[success] Total time: 0 s, completed 18-feb-2017 13:24:25
```

Say that, when we type task3 the following should happen: 

- first task1 should execute,
- then task2
- then task3

How do we do that? Lets find out.

### Dependency Key Operator
As you may or may not know, Sbt is being simplified which means that a lot of 'exotic operators' are being dropped and only a few operators are being used and in context of a certain use case can be applied. Some of those operators you already know like ':=', '+=', '++=' and so on. 

Because of a [technical reason #1444](https://github.com/sbt/sbt/issues/1444) we still need to use the '<<=' operator which is the 'Dependency Key' operator for some dependencies. No problem if you know what it is and what it does.

Sbt allows us to define the following dependencies between tasks:

- dependsOn: a task depends on another task,
- triggeredBy: a task is triggered by another task,
- runBefore: a task is run before another task

For example, we have the previously defined three tasks, and we also have defined a dependency between them:

```scala
lazy val task1 = taskKey[Unit]("task 1")

lazy val task2 = taskKey[Unit]("task 2")

lazy val task3 = taskKey[Unit]("task 3")

task1 := println("Task 1")

task2 := println("Task 2")

task3 := println("Task 3")

task3 := (task3 dependsOn task2 dependsOn task1).value
```

When we run task3, which, beside the implementation also has a dependency rule defined as we can see above, the following will happen:

```bash
> task3
Task 1
Task 2
Task 3
[success] Total time: 0 s, completed 18-feb-2017 13:24:55
```

The rule `task3 := (task3 dependsOn task2 dependsOn task1).value` is the new syntax and will be supported by newer versions of sbt.The following syntax will also work but is deprecated:

Note: If you are using SBT 1.0 or higher, please replace '<<=' with ':='.

```scala
// define a dependency rule using the '<<=' syntax which is deprecated
task3 <<= task3 dependsOn task2 dependsOn task1
```

Lets say we want to define the following, I want task1 to be run and then task3 when I type task1. How do we do that? 

```
lazy val task1 = taskKey[Unit]("task 1")

lazy val task2 = taskKey[Unit]("task 2")

lazy val task3 = taskKey[Unit]("task 3")

task1 := println("Task 1")

task2 := println("Task 2")

task3 := println("Task 3")

// when I type 'task1': task1 -> task3, because task3 is triggeredBy task1
task3 <<= task3 triggeredBy task1
```

Lets try it out:

```bash
> task1
Task 1
Task 3
[success] Total time: 0 s, completed 18-feb-2017 13:42:53
> task2
Task 2
[success] Total time: 0 s, completed 18-feb-2017 13:42:55
> task3
Task 3
[success] Total time: 0 s, completed 18-feb-2017 13:42:56
```

Note: As of SBT v1.0, replace '<<=' with ':='.

What has happened here is that the rule `task3 <<= task3 triggeredBy task1` that uses the deprecated '<<=' 'dependency key' operator and we must use it because of technical reasons, has as effect that when we type 'task1', first 'task1' will run and because 'task1' runs, 'task3' will be triggered causing task3 to also be run.

Lets say, I want to define the following, I want task1 to be run and then task3 when I type 'task3', so task1 must run before task3. How do we do that?

```scala
lazy val task1 = taskKey[Unit]("task 1")

lazy val task2 = taskKey[Unit]("task 2")

lazy val task3 = taskKey[Unit]("task 3")

task1 := println("Task 1")

task2 := println("Task 2")

task3 := println("Task 3")

task1 <<= task1 runBefore task3
```

Lets try it out:

```bash
> task1
Task 1
[success] Total time: 0 s, completed 18-feb-2017 13:45:44
> task2
Task 2
[success] Total time: 0 s, completed 18-feb-2017 13:45:45
> task3
Task 1
Task 3
[success] Total time: 0 s, completed 18-feb-2017 13:45:47
```

### Scopes
Key -> Value pairs play an important role in Sbt as they let us define settings and settings let us configure our build. Keys can easily be configured so that they have a value in a specific Configuration, Task or (Configuration,Task) combination. It is up to the implementation of the Task that needs a the configured value whether or not it looks for the value, so we have to look at the source code of the task for that.

Sbt gives us shorthands so easily scope Keys. There are two Scopes `Global` and `ThisBuild`. 

### ThisBuild Scope
Lets first task about 'ThisBuild'. When a Build consists of multiple projects, then the Scope 'ThisBuild' is handy. For a single project 'build.sbt' if you will, the Scope 'ThisBuild' doesn't make much sense, as the configuration will apply for the single (default) project.

Say we have a multi-project `build.sbt` like so:

```scala
lazy val project1 = project in file("project1")

lazy val project2 = project in file("project2")
```

If we want to query settings for a specific project we would type the following in the sbt console:

```bash
project1/name
[info] project1

> project1/scalaVersion
[info] 2.10.6
```

Until now we haven't seen this syntax. The project name is also part of a Key. So the fully qualified name of a key is really (project/config:task::setting)

Of course we can leave parts of like so:

```bash
> project1/scalaVersion
[info] 2.10.6
> project1/test:scalaVersion
[info] 2.10.6
> project1/test:test::scalaVersion
[info] 2.10.6
```

Now lets say that we want to configure the scalaVersion only for 'project1' then we would type:

```
set scalaVersion in project1 := "2.11.8"
[info] Reapplying settings...
> project1/scalaVersion
[info] 2.11.8

> project2/scalaVersion
[info] 2.10.6
```

We could also set the scalaVersion for project1 in the `build.sbt` like so:

```scala
lazy val project1 = (project in file("project1")).settings(scalaVersion := "2.12.1")

lazy val project2 = project in file("project2")
```

Then query:

```bash
> project1/scalaVersion
[info] 2.12.1
```

Now say that we want to set the scalaVersion for all projects in our Build, then we would configure:

```bash
> set scalaVersion in ThisBuild := "2.11.8"
[info] Reapplying settings...

> project1/scalaVersion
[info] 2.12.1

> project2/scalaVersion
[info] 2.11.8
```

Because we haven't removed the specific configuration that we have set on 'project1', that scalaVersion is still '2.12.1' but the setting for 'project2' has changed. The scope 'ThisBuild' is shorthand for the following:

```
All projects and all configuration and all tasks in the current build only.
```

### Global Scope
The scope Global is handy to define settings that apply to all projects everywhere on your computer or your enterprise, and all of there configurations and all of there tasks. I guess that covers 'Global'. This scope only makes sense if you create plugins and you want to add the setting to all projects everywhere. If you can remember that Keys are scoped on Axis, so (Project/Configuration:Task) then the difference between the scope 'ThisBuild' and 'Global' is that for 'ThisBuild' the axis looks like ({.}/*:*) and for Global the axis looks like (*/*:*).

## Parsing user input
SBT supports [parsing user input](http://www.scala-sbt.org/0.13/docs/Parsing-Input.html) as part of a task. To parse use input we use the `inputKey` eg:

```scala
import sbt.complete.DefaultParsers._

val hello = inputKey[String]("Hello World")

hello := {
  val name: String = (Space ~> StringBasic).parsed
  val greeting = s"Hello $name"
  streams.value.log.info(greeting)
  greeting
}
```

Sbt uses the sbt-parser-combinator library and parsing user input uses a combination of parsers defined in the
standard library and your own custom parsers. I have a [study project](https://github.com/dnvriend/sbt-parser-test) that shows how you can use the sbt parser
library and how to build your own parsers.

It is possible to reuse the `inputTask` by another task, to do that you could do the following:

```scala
val useHello = taskKey[String]("Using hello")

useHello := {
  val result = hello.toTask(" Dennis").value
  val msg = s"useHello: '$result'"
  streams.value.log(msg)
  msg
}
```

Not that we must use a space, as our parser states that the user input should start with a Space as defined in:

```scala
val name: String = (Space ~> StringBasic).parsed
```

### Common SBT Commands
The following sbt commands are handy to know. Of course you can create your own tasks and query for which tasks are available
by typing `sbt tasks -V`:

```bash
sbt help                                     # Prints a help summary.
sbt about                                    # Displays basic information about sbt and the build.
sbt tasks                                    # Displays the main tasks defined directly or indirectly for the current project.
sbt tasks -V                                 # Displays all tasks
sbt settings                                 # Displays the main settings defined directly or indirectly for the current project.
sbt settings -V                              # Displays all settings
sbt projects                                 # List the names of available builds and the projects defined in those builds.
sbt project                                  # Displays the name of the current project.
sbt project /                                # Changes to the initial project.
sbt project name                             # Changes to the project with the provided name.
sbt run                                      # Runs a main class, passing along arguments provided on the command line
sbt runMain com.github.dnvriend.HelloWorld   # Runs the main class selected by the first argument, passing the remaining arguments to the main method.
sbt console                                  # Starts the Scala interpreter with the project classes on the classpath.
sbt compile                                  # Compiles sources.
sbt clean                                    # Deletes files produced by the build, such as generated sources, compiled classes, and task caches.
sbt test                                     # Executes all tests.
sbt testOnly PersonTest                      # Executes the tests provided as arguments or all tests if no arguments are provided.
sbt ";clean;compile;run"                     # Runs the specified commands.
```

### SourceGenerators
The [sourceGenerators](https://github.com/sbt/sbt/blob/0.13/main/src/main/scala/sbt/Keys.scala#L115) setting defines a list of
tasks that generate sources. A source generation task should generate sources in a subdirectory of `sourceManaged`
and return a sequence of files generated.

 The key to add the task to is called `sourceGenerators`. Because we want to add the task, and not the value after its execution,
we use `taskValue` instead of the usual `value`. It should be scoped according to whether the generated files are main (Compile)
or test (Test) sources.

For example, lets say we want to generate an `BuildInfo.scala` file that contains information about our build. We can do the following:

- create a task 'getBuildInfo' that aggregates information about our build,
- create a task 'makmakeBuildInfo' that will create the 'Information.scala' file in the sourceManaged dir and stores the
  information that has been aggregated by 'getBuildInfo' into that file
- create a 'main.Main' console application that uses the 'BuildInfo.scala' file
- run the console application

```scala
val getCommitSha = taskKey[String]("Returns the current git commit SHA")

getCommitSha := {
  Process("git rev-parse HEAD").lines.head
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
```

We need a console application to test it with so put the following class in 'src/main/scala/main':

```scala
package main

object Main extends App {
  println(build.BuildInfo.info)
}
```

Run the application with 'sbt run'.

### ResourceGenerators
The [resourceGenerators](https://github.com/sbt/sbt/blob/0.13/main/src/main/scala/sbt/Keys.scala#L116) setting defines a list of
tasks that generate resources. A resource generation task should generate resources in a subdirectory of `resourceManaged`
and return a sequence of files generated.

The key to add the task to is called `resourceGenerators`. Because we want to add the task, and not the value after its execution,
we use `taskValue` instead of the usual `value`. It should be scoped according to whether the generated files are main (Compile)
or test (Test) resources.

For example, lets say that we want to get the git commit hash of our project and save it in a Typesafe config file named
'version.config' and put it in the 'resourceManaged' directory, we can do the following:

- create a task 'gitCommitSha' that queries 'git' and parses the response and returns the git hash as a String
- create a task 'makeVersionConfig' that will create the 'version.config' file in the resourceManaged dir and stores the
  git commit hash in that file
- create a 'main.Main' console application that uses the 'version.config' file
- run the console application

Lets first create the two tasks, you can put the following in 'build.sbt':

```scala
// we need the typesafe-config library
libraryDependencies += "com.typesafe" % "config" % "1.3.1"

// 'gitCommitSha' will query 'git' for the SHA of HEAD
val gitCommitSha = taskKey[String]("Returns the current git commit SHA")

gitCommitSha := {
  Process("git rev-parse HEAD").lines.head
}

// 'makeVersionConfig' will create the 'version.config' file
val makeVersionConfig = taskKey[Seq[File]]("Makes a version config file")

makeVersionConfig := {
  println("Creating makeVersionConfig")
  val resourceDir: File = (resourceManaged in Compile).value
  val configFile: File = new File(resourceDir, "version.config")
  val gitCommitValue: String = gitCommitSha.value
  val content = s"""commit-hash="$gitCommitValue""""
  IO.write(configFile, content)
  Seq(configFile)
}

// add the 'makeVersionConfig' Task to the list of resourceGenerators.
// resourceGenerators is of type: SettingKey[Seq[Task[Seq[File]]]] which
// means that we can add, well, resourceGenerators to it,
// like our 'makeVersionConfig' which is a resourceGenerator.
resourceGenerators in Compile += makeVersionConfig.taskValue
```

We need a console application to test it with so put the following class in 'src/main/scala/main':

```scala
package main

import com.typesafe.config.ConfigFactory

import scala.io.Source

object Main extends App {
  val config = ConfigFactory.parseURL(getClass.getResource("/version.config"))
  val hashFromConfig = config.getString("commit-hash")
  val versionConfigFileAsString = Source.fromURL(getClass.getResource("/version.config")).mkString
  println(
    s"""
      |versionConfigFile: $versionConfigFileAsString
      |hashFromConfig: $hashFromConfig
    """.stripMargin)
}
```

Run the application with 'sbt run'.

## Plugins
tbd


## intellij sbt plugin
- [Scala plugin for IntelliJ IDEA 2017.1](https://blog.jetbrains.com/scala/2017/03/23/scala-plugin-for-intellij-idea-2017-1-cleaner-ui-sbt-shell-repl-worksheet-akka-support-and-more/)
- [Scala plugin Bugs](https://intellij-support.jetbrains.com/hc/en-us/community/topics/200381545-Scala)


### The SBT-shell
see: https://youtrack.jetbrains.com/issue/SCL-10984
The [sbt-shell](https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000117230-Sbt-shell-for-build-an-import) attempts
to use the sbt shell as a server for running builds and project imports. It's marked as experimental since the communication
doesn't happen over a proper server protocol (this will only be supported for sbt 1.0) but instead by literally pasting
commands into the integrated shell and parsing the output.

Have fun!
