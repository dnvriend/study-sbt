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
What is the [Build](http://www.scala-sbt.org/1.x/docs/Basic-Def.html#What+is+a+build+definition%3F)? Well, the build is just a collection of [Project](http://www.scala-sbt.org/1.x/api/sbt/Project.html)s that Sbt should build. We as developers identify to Sbt which projects there are, where they are located and what their names are. When we do not specify projects, sbt will assume a single project in the current directory, but we as developers can define other projects that Sbt should build. So the build says something about one or more projects that Sbt should build. 

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

We can show the result of either a Setting or a Task using the sbt-console with the help of the ['show'](http://www.scala-sbt.org/1.x/docs/Basic-Def.html#Keys+in+sbt+shell) command. For example, when we type 'show name', sbt will evaluate the Key 'name' and return the evaluated value. Of course, because 'name' is a Setting, the initialization has already been done when Sbt started, so it will return the value immediately:

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

The Task clean evaluates to the value ['()' of type Unit](http://www.scala-lang.org/api/2.12.3/scala/Unit.html) which is returned, because it does side effects like deleting the contents of the './target' directory.

### Recap until now
So a Build contains one or more projects. A project defines itself using settings. A Setting is just a Key -> Value pair that is initialized only once and a Task is a Key -> Value pair that will be evaluated on demand.

### Configurations
I will assume that you already know a little how sbt works and are already working with it so you know that sbt supports testing. For unit testing you will need for example the [ScalaTest](http://www.scalatest.org/) and if you are creating reactive applications, the [akka-testkit](https://doc.akka.io/docs/akka/2.5.4/scala/testing.html) library as a dependency. Also, we have split the code that is for testing from our business code. The code bases have different paths, eg. the test code exists in './src/main/test' and this code base has a dependency with the test libraries that our business code doesn't have. 

Sbt uses *Configurations* to segment Settings so it knows which setting to use when a certain task is being executed. There are a lot of Configurations defined in Sbt and you can also define your own. We will look into those a little bit later. 

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

So the key 'sourceDirectories' has a different value for different [scopes](http://www.scala-sbt.org/1.x/docs/Scopes.html) and it depends on *the implementation of the Task* where it looks to get the value of a Key. In our examples we specifically look for a value for `(sourceDirectories in Test).value` to get the value and for `(sourceDirectories in Compile).value`. 

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

Of course, a setting or a task does not have to exist in a certain Configuration like eg. the task 'test' does exist in the Configuration 'test' (of course):

```bash
> test:test
[info] Done updating.
[success] Total time: 1 s, completed Oct 31, 2017 7:05:17 AM
```

But the task 'test' does not exist in the Configuration 'compile':

```bash
> compile:test
[error] No such setting/task
[error] compile:test
[error]
```

But when we type the following:

```bash
> test
[success] Total time: 0 s, completed Oct 31, 2017 7:09:01 AM
```

The task 'test' works without specifying the 'test' configuration like above we typed 'test:test', how come? You can define settings and tasks specific for a Configuration like 'Test' or 'Compile', but you can also define settings and tasks that apply for all configurations. The task 'test' is made available in all scopes. In this case that is reasonable, because it is very handy to just type 'test' and have the test task executed.

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

And lets change the name in the Configuration 'Compile':

```scala
set name in Compile := "study-sbt-in-compile"
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
[info] study-sbt-in-compile
```

Note: In Sbt older than v1.0 the key-in-configuration fallback value resolution was a bit buggy and the fallback value resolution didn't always work as expected.

### Configuration by Task
A configuration can also be scoped to a specific *Task* for example add the following to build.sbt:

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

And now also comment out the line:

```scala
mysetting in Test := "mysetting for the current project, for the Test configuration and all tasks"
```

Sbt will use the next fallback and so on:

```bash
> MyTask
mysetting for the current project, all configurations and all tasks
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

### Inspecting Sbt Settings
You've learned a lot about Sbt, what a build is, that tasks and settings are and also about scopes. What I want you to do now is take 15 minutes of your time to read the explanation on [Inspecting Settings](http://www.scala-sbt.org/1.x/docs/Inspecting-Settings.html) from the [Sbt documentation](http://www.scala-sbt.org/documentation.html). When you can inspect your settings and understand what the effective settings and tasks will be used you will have a better Sbt development experience in general.

Lets take a closer look at inspecting a task. The following example shows three tasks where task3 is dependent on task2 and task1:

```scala
lazy val task1 = taskKey[Unit]("task 1")
lazy val task2 = taskKey[Unit]("task 2")
lazy val task3 = taskKey[Unit]("task 3")

task1 := println("Task 1")
task2 := println("Task 2")
task3 := println("Task 3")

task3 := (task3 dependsOn task2 dependsOn task1).value
```

If we inspect the build, we see the following dependencies:

```bash
sbt:study-sbt> inspect task3
[info] Task: Unit
[info] Description:
[info] 	task 3
[info] Provided by:
[info] 	{file:/Users/dennis/projects/study-sbt/}study-sbt/*:task3
[info] Dependencies:
[info] 	*:task2
[info] 	*:task1
```

We see that task3 is dependent on task2 and task2 and this is also the evaluation order. If we change the dependency that task3 is dependent on task1 that depends on task2 we see the following:

```scala
task3 := (task3 dependsOn task1 dependsOn task2).value
```

After a reload, lets inspect task3:

```bash
sbt:study-sbt> inspect task3
[info] Task: Unit
[info] Description:
[info] 	task 3
[info] Provided by:
[info] 	{file:/Users/dennis/projects/study-sbt/}study-sbt/*:task3
[info] Dependencies:
[info] 	*:task1
[info] 	*:task2
```

We have changed the sequence of the dependencies.

Now lets take a quick look at a full inspect output. It contains the following:

- __Provided by__: shows the actual scope (the full scope) where the setting is defined
- __Dependencies__: list all the inputs to a task. The listing is the sequence of evaluation and if possible, sbt will try to evaluate the dependencies in parallel. The entries show the configuration, task and setting that is an input for the task. Please note that these entries are the defined inputs with scope. To see the 'actual' values, please use the `inspect actual <key>` command,
- __Delegatess:__ A setting has a key and a scope. A request for a key in a scope 'A' may be delegated to another scope if 'A' doesn’t define a value for the key. The delegation chain is well-defined and is displayed in the Delegates section of the inspect command. The Delegates section shows the order in which scopes are searched when a value is not defined for the requested key,
- __Related__: lists all of the definitions of a key so read these listings as 'there are also these keys in these scopes you can take a look at',

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

Notice:
- For SBT < v1.0 users: Because of a [technical reason #1444](https://github.com/sbt/sbt/issues/1444) we still need to use the '<<=' operator which is the 'Dependency Key' operator for some dependencies. No problem if you know what it is and what it does.
- For SBT >= 1.0 users: Note: please replace '<<=' with ':=' as the dependency operator.

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
task3 := (task3 triggeredBy task1).value
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

task1 := (task1 runBefore task3).value
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

### Task graphs
The Sbt documentation has very good documentation on the [task graph](http://www.scala-sbt.org/1.x/docs/Task-Graph.html), but here is the short explanation.

The `.value` method is used to express a dependency on another task or setting. The `.value` method is special and may only be called in the argument to `:=`, `+=`, or `++=`.

The `.value` method is not a normal Scala method and it is important to understand this. Sbt looks for these `.value` methods and lifts these operations outside the task body and will all be evaluated first. For developers, working with the `.value` methods within methods can lead to unexpected behavior because, independent on where the `.value` call is placed in a method, or even within an if-then-when expression, all `.value` methods will be lifted outside the body and evaluated first.

Moreover, the evaluation order of all these `.value` methods are non-determinstic. So what is the best way to work with the `.value` method to write tasks? There are some options:

1. Inlining `.value`
2. Not inlining `.value`

### Inlining '.value'
Lets look at the first strategy 'Inlining .value'. We can inline the `.value` method by putting all calls to `.value` at the top of the task like in the following examples. The effect is that the developer cannot make a mistake by using the `.value` calls inside an if-then-else call. Also you have all calls to `.value` grouped together at the top of the task, which makes the separation clear. Lets build up to this style of tasks:

```scala
lazy val task1 = taskKey[Unit]("")

task1 := {
  // put all .value calls here at the top


  // put your task logic down below here
  println("Hello")
}
```

The previous code creates a task `task1` that has no dependencies, lets take a look:

```bash
sbt:study-sbt> inspect task1
[info] Task: Unit
[info] Description:
[info]
[info] Provided by:
[info] 	{file:/Users/dennis/projects/study-sbt/}study-sbt/*:task1
[info] Defined at:
[info] 	/Users/dennis/projects/study-sbt/build.sbt:7
[info] Delegates:
[info] 	*:task1
[info] 	{.}/*:task1
[info] 	*/*:task1
```

Lets make task1 dependent on clean:

```scala
lazy val task1 = taskKey[Unit]("")

task1 := {
  // put all .value calls here at the top
  clean.value

  // put your task logic down below here
  println("Hello")
}
```

We now see a dependency on the clean task:

```bash
sbt:study-sbt> inspect task1
[info] Task: Unit
[info] Description:
[info]
[info] Provided by:
[info] 	{file:/Users/dennis/projects/study-sbt/}study-sbt/*:task1
[info] Defined at:
[info] 	/Users/dennis/projects/study-sbt/build.sbt:7
[info] Dependencies:
[info] 	*:clean
[info] Delegates:
[info] 	*:task1
[info] 	{.}/*:task1
[info] 	*/*:task1
```

Lets add another dependency, to `update` for example:

```scala
lazy val task1 = taskKey[Unit]("")

task1 := {
  // put all .value calls here at the top
  clean.value
  val updateReport: UpdateReport = update.value

  // put your task logic down below here
  println(updateReport.allConfigurations)
}
```

We now see a second dependency also on the update task:

```bash
sbt:study-sbt> inspect task1
[info] Task: Unit
[info] Description:
[info]
[info] Provided by:
[info] 	{file:/Users/dennis/projects/study-sbt/}study-sbt/*:task1
[info] Defined at:
[info] 	/Users/dennis/projects/study-sbt/build.sbt:7
[info] Dependencies:
[info] 	*:update
[info] 	*:clean
[info] Delegates:
[info] 	*:task1
[info] 	{.}/*:task1
[info] 	*/*:task1
```

By splitting the code into 'all calls to .value' and 'task logic', the developer doesn't have to worry about the evaluation order of all values and mixing the Sbt macro logic and the task logic.

### Not-inlining '.value'
Lets look at the second strategy, not-inlining `.value` methods. Here we will create a very distinct separation between the task logic and the task itself. We'll put all the logic inside a module that will be called by the task. The dependencies on the values will be part of the build script as a short implementation that will only make `.value` calls. Lets take a look at this strategy:

```scala
lazy val task1 = taskKey[Unit]("")
task1 := task1Impl(update.value, streams.value.log, name.value, scalaVersion.value)

def task1Impl(updateReport: UpdateReport, log: Logger, projectName: String, scalaVersion: String): Unit = {
  log.info(
    s"""
      |ProjectName: $projectName
      |ScalaVersion: $scalaVersion
      |Report: ${updateReport.stats}
    """.stripMargin)
}
```

We've split the task into method that contains all 'materialized' values from the Sbt environment. The developer now doesn't have to worry about the macro details of Sbt. The sequence evaluation works as expected inside the `task1Impl` method. The task implementation, which is the line 'task1 := task1Impl(update.value, streams.value.log, name.value, scalaVersion.value)', is responsible for creating the dependency on all other tasks an settings an evaluating all values. When all values are evaluated, the `task1Impl` method will be called.

Lets look at the dependencies:

```bash
sbt:study-sbt> inspect task1
[info] Task: Unit
[info] Description:
[info]
[info] Provided by:
[info] 	{file:/Users/dennis/projects/study-sbt/}study-sbt/*:task1
[info] Defined at:
[info] 	/Users/dennis/projects/study-sbt/build.sbt:7
[info] Dependencies:
[info] 	*:scalaVersion
[info] 	*:name
[info] 	*:task1::streams
[info] 	*:update
[info] Delegates:
[info] 	*:task1
[info] 	{.}/*:task1
[info] 	*/*:task1
```

As expected, we have dependencies with all tasks and settings we have called `.value` on.

A second strategy is to use an Scala object that will be used to be a container for the method. The only downside of this strategy is that the object must be put inside the `project` directory of your project. Just put it beside your `build.properties` file. Lets create the file `project/Task1Module.scala` with the following contents:

```scala
import sbt.{Logger, UpdateReport}

object Task1Module {
  def task1Impl(updateReport: UpdateReport, log: Logger, projectName: String, scalaVersion: String): String = {
    s"""
       |ProjectName: $projectName
       |ScalaVersion: $scalaVersion
       |Report: ${updateReport.stats}
    """.stripMargin
  }
}
```

This implementation returns a String, in the previous example the method returned nothing. Lets look at the contents of `build.sbt`:

```scala
val task1 = taskKey[String]("")
task1 := Task1Module.task1Impl(update.value, streams.value.log, name.value, scalaVersion.value)
```

Please note that I've changed `task1` a little, it now expects a String to be returned that we can show on the Sbt console with the command `show task1`:

```bash
sbt:study-sbt> show task1
[info]
[info] ProjectName: study-sbt
[info] ScalaVersion: 2.12.4
[info] Report: Resolve time: 64 ms, Download time: 4 ms, Download size: 0 bytes
[info]
```

Lets inspect task1:

```bash
sbt:study-sbt> inspect task1
[info] Task: java.lang.String
[info] Description:
[info]
[info] Provided by:
[info] 	{file:/Users/dennis/projects/study-sbt/}study-sbt/*:task1
[info] Defined at:
[info] 	/Users/dennis/projects/study-sbt/build.sbt:3
[info] Dependencies:
[info] 	*:scalaVersion
[info] 	*:name
[info] 	*:task1::streams
[info] 	*:update
[info] Delegates:
[info] 	*:task1
[info] 	{.}/*:task1
[info] 	*/*:task1
```

As expected, the task still has dependencies to other settings and tasks like before.

### Parallel and sequential task execution
Sbt tries to execute tasks parallel by default. Most tasks can be evaluated in parallel like for example the following example:

```scala
lazy val task1 = taskKey[String]("t1")
lazy val task2 = taskKey[String]("t2")
lazy val task3 = taskKey[String]("t3")
lazy val runAll = taskKey[String]("all parallel (the default behavior)")

task1 := {
  Thread.sleep(1000)
  println("t1")
  "task1"
}

task2 := {
  Thread.sleep(750)
  println("t2")
  "task2"
}

task3 := {
  Thread.sleep(850)
  println("t3")
  "task3"
}

runAll := {
  val t1 = task1.value
  val t2 = task2.value
  val t3 = task3.value
  val all = s"$t1 - $t2 - $t3"
  println(all)
  all
}
```

When the `runAll` task is evaluated, Sbt evaluates the tasks in parallel and the value will be stored in the variables. When all tasks have been evaluated,
the String van be evaluated and stored in `all` and lastly it can be printed to the console.

The default `parallel` behavior is a feature of Sbt and cannot easily be disabled. Of cource, any task dependencies on other tasks are maintained when
executing the tasks.

### Sequentially executing tasks
When it is necessary to evaluate tasks sequentially, for example, when orchestrating a deployment or forcing tasks to be executed sequentially, Sbt v0.13 and later have support for this using the `Def.sequential` task:

```scala
lazy val runAllSequential = taskKey[String]("all sequential (forced by use of Def.sequential().value")

runAllSequential := Def.sequential(task1, task2, task3).value
```

When the `runAllSequential` task is evaluated, the tasks will be executed sequentially. This operation is created using Scala `macros`, which means
you can only use Def.sequential as the way we do above. You cannot use it inside another `Def.task` or `Def.taskDyn` etc.

### Returning a task based on a setting
A task can return a different task based on a value of a setting:

```scala
lazy val choice = settingKey[String]("The task to execute")
choice := "t1"

lazy val staticChoice = taskKey[Unit]("")
staticChoice := Def.taskDyn {
  choice.value match {
    case "t1" => task1.toTask
    case "t2" => task2.toTask
    case "t3" => task3.toTask
    case "all" => runAll.toTask
    case _ => runAllSequential.toTask
  }
}.value
```

Based on the value of `choice`, the `Def.taskDyn` function returns a Task to be evaluated. Please note that the `Def.sequential` task is
referenced by using its key-name which is `runAllSequential` in this example. You cannot use the `Def.sequential`inline.

Please note the `.value` call at the end of `Def.taskDyn`, it is easy to forget.

### Returning a task based on user input
A task can return a different task based on user input:

```scala
lazy val inputChoice = inputKey[Unit]("")
inputChoice := Def.inputTaskDyn {
  Def.spaceDelimited("choice").parsed.head match {
    case "t1" => task1.toTask
    case "t2" => task2.toTask
    case "t3" => task3.toTask
    case "all" => runAll.toTask
    case "seq" => runAllSequential.toTask
    case unknown => Def.task {
      streams.value.log.info(s"(inputChoice): Unknown task: '$unknown'")
      unknown
    }
  }
}.evaluated
```

Based on user input, the `Def.inputTaskDyn` function returns a Task to be evaluated. Please note that the `Def.sequential` task is
referenced by using its key-name which is `runAllSequential` in this example. You cannot use the `Def.sequential`inline.

Please note the `.evaluated` call at the end of `Def.inputTaskDyn`, it is easy to forget.

### Returning Classes from the classDirectory
Compiled classes are available in `classDirectory in Compile` setting. The following code can help getting a list of compiled classes as String and also
as a `Seq[Class[_]]`:

```scala
lazy val allClassesInClassDirectory = taskKey[Seq[(String, String)]]("Returns all classes in the classDirectory")
allClassesInClassDirectory := {
  import scala.tools.nsc.classpath._
  val baseDir: File = (classDirectory in Compile).value
  val allClassFilesInClassDir: Seq[File] = (baseDir ** "*.class").get
  val relativizer = IO.relativize(baseDir, _: File)
  allClassFilesInClassDir
    .flatMap(relativizer(_).toSeq)
    .map(FileUtils.stripClassExtension)
    .map(_.replace("/", "."))
    .map(PackageNameUtils.separatePkgAndClassNames)
}

lazy val allObjectsInClassDirectory = taskKey[Seq[(String, String)]]("Returns all objects in the classDirectory")
allObjectsInClassDirectory := {
  allClassesInClassDirectory.value.filterNot {
    case (_, className) => className.endsWith("$")
  }
}

lazy val onlyClassesInClassDirectory = taskKey[Seq[(String, String)]]("Returns only classes in the classDirectory")
onlyClassesInClassDirectory := {
  allClassesInClassDirectory.value.filterNot {
    case (_, className) => className.contains("$")
  }
}

lazy val allClassesInClassDirectoryAsClass = taskKey[Seq[Class[_]]]("Returns all classes in the classDirectory as Class[_]")
allClassesInClassDirectoryAsClass := {
  val cp: Seq[File] = (fullClasspath in Compile).value.map(_.data)
  val cl = sbt.internal.inc.classpath.ClasspathUtilities.makeLoader(Seq((classDirectory in Compile).value) ++ cp, scalaInstance.value)
  allClassesInClassDirectory.value.map {
    case (packageName, className) => cl.loadClass(s"$packageName.$className")
  }
}

lazy val onlyClassesInClassDirectoryAsClass = taskKey[Seq[Class[_]]]("Returns only classes in the classDirectory as Class[_]")
onlyClassesInClassDirectoryAsClass := {
  val cl = sbt.internal.inc.classpath.ClasspathUtilities.makeLoader(Seq((classDirectory in Compile).value), scalaInstance.value)
  onlyClassesInClassDirectory.value.map {
    case (packageName, className) => cl.loadClass(s"$packageName.$className")
  }
}
```

### Getting annotated classes from the classpath
There are [different kinds of annotations in Scala](https://stackoverflow.com/questions/35420334/how-to-define-and-use-custom-annotations-in-scala):

- Plain annotations that are only in the code: These can be accessed from macros in the compilation unit where the macro is called where the macro gets access to the AST
- StaticAnnotations that are shared over compilation units: these can be accessed via scala reflection api
- ClassfileAnnotations: these represent annotations stored as java annotations. If you want to access them via the Java Reflection API you have to define them in Java though.

A good read about the subject is: [What is the (current) state of scala reflection capabilities, especially wrt annotations, as of version 2.11?](https://stackoverflow.com/questions/35251426/what-is-the-current-state-of-scala-reflection-capabilities-especially-wrt-ann/35254466#35254466)

[Java annotations](https://docs.oracle.com/javase/tutorial/java/annotations/) are a form of metadata that provides extra information about a program but is not part of the program itself. Annotations have no direct effect on the code they annotate.

Annotations have a number of uses, among them:

- Corrections of encoding:
  - Information for the compiler: Annotations can be used by the compiler to detect errors or suppress warnings,
  - Show deprecation warnings,
  - validation of correction of encodings like the '@tailrec' annotation to check for tail-recursive methods,
- Compile-time and deployment-time processing: Software tools can process annotation information to generate code, property files, and so forth,
- Runtime processing: some annotations are available to be examined at runtime.

As described in [A tour of Scala: Annotations](https://docs.scala-lang.org/tour/annotations.html), Annotations associate meta-information with definitions. Creating an annotation is quite simple, for example, lets create our own Java Annotation:

```java
package main;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MyMarker {
    String name() default "N/A";
    String author() default "Dennis Was Here";
}
```

Annotatations can be handy, but my default the JVM does some memory-saving optimizations, which means that an annotation is not available at runtime by default. This means that we have
to 'annotate' the annotation with an annotation that tells the compiler that the annotation must be retained for runtime use, which is what we want.

The default retention policy is `RetentionPolicy.CLASS` which means that, by default, annotation information is not retained at runtime:

> Annotations are to be recorded in the class file by the compiler but need not be retained by the VM at run time. This is the default behavior.

Instead, use `RetentionPolicy.RUNTIME`:

> Annotations are to be recorded in the class file by the compiler and retained by the VM at run time, so they may be read reflectively.

We will use the `getClass[AnnotatedClass].getAnnotations` method that only returns Java Annotations. To find which classes have been annotated with the `MyMarker` annotation, we can do the following:

```scala
lazy val findMarked = taskKey[Seq[Class[_]]]("Returns the classes that have been annotated with the 'MyMarker' annotation")
findMarked := {
  allClassesInClassDirectoryAsClass.value
      .filter(_.getDeclaredAnnotations.toList.exists(_.annotationType().getName.contains("MyMarker")))
}
```

As a side-note: `class.getDeclaredAnnotations` and `class.getAnnotations` is that with `getDeclaredAnnotations` it ignores inherited annotations, so it would only return annotations that are declared on the class itself, the `getAnnotations` method would return all annotations also inherited ones.

We can also parse the annotation like so:

```scala
lazy val parseMarked = taskKey[Seq[(Class[_], String)]]("Returns the classes that have been annotated with the 'MyMarker' annotation with the JSON representation of the fields of the annotation")
parseMarked := {
  def getMarkedAnnotationValues(cl: Class[_]): Option[(Class[_], String)] = {
    cl.getDeclaredAnnotations.toList.find(_.annotationType().getName.contains("MyMarker")).map { anno =>
      val name = anno.annotationType().getMethod("name").invoke(anno)
      val author = anno.annotationType().getMethod("author").invoke(anno)
      s"""{"name":"$name","author":"$author"}"""
    }.map(json => (cl, json))
  }
  findMarked.value.flatMap(getMarkedAnnotationValues)
}
```

### Unbound settings and tasks
The keys of settings and tasks don't have to be bound ie. they dont have to have an implementation. Sbt has operators that can determine whether
or not the keys are bound, and if not, give us a way to choose alternate keys or implementations to use:

```scala
lazy val s1 = settingKey[String]("s1")
// there is no implementation of s1
// so the key 's1' is "not bound"
//s1 := "foo"

lazy val s2 = settingKey[String]("s2")
s2 := "bar"

lazy val t1 = taskKey[Unit]("")
t1 := {
  // s1 is not bound, so maybeS1 is None
  val maybeS1: Option[String] = s1.?.value
  assert(maybeS1.isEmpty)

  // s1 is not bound, if so, use 'quz' as string
  val alternativeValue: String = s1.??("quz").value
  assert(alternativeValue == "quz")

  // if s1 is not bound, use the value of setting 's2'
  val effectiveSetting: String = s1.or(s2).value
  assert(effectiveSetting == "bar")
}
```

### Changing the setting dynamically
A setting can return a different value based on a result for example a value or a setting, or it could be calling another task and based on some
observed effect (a file that exists or something else), change the setting's value:

```scala
lazy val s2 = settingKey[String]("s2")
s2 := "bar"

lazy val s3 = settingKey[String]("s2")
s3 := Def.settingDyn {
  s2.value match {
    case "bar" => Def.setting("foo")
    case _ => Def.setting("bar")
  }
}.value

lazy val t1 = taskKey[Unit]("")
t1 := {
  println("effective: " + s3.value)
}
```

Please note the `.value` call at the end of `Def.settingDyn`, it is easy to forget. Also note the we are using `Def.setting` here, until now we only
have been using 'Def.task', but that doesn't work here.

### Getting user input
There are many ways for getting the users input, for example, using the Parser Combinator libary of sbt, but the following way is also very easy and has
been taken from the [AWS Lambda Plugin](https://github.com/quaich-project/quartercask/blob/master/lambda/src/main/scala/codes/bytes/quartercask/lambda/AWSLambdaPlugin.scala)
example:

```scala
lazy val t1 = taskKey[Unit]("")
t1 := {
  val name = readInput("What is your name?")
  val age = readInput("What is your age?")
  streams.value.log.info(s"Hello '$name', you are '$age' years old!")
}

def readInput(prompt: String): String = {
  SimpleReader.readLine(s"$prompt\n") getOrElse {
    val badInputMessage = "Unable to read input"
    val updatedPrompt = if (prompt.startsWith(badInputMessage)) prompt else s"$badInputMessage\n$prompt"
    readInput(updatedPrompt)
  }
}
```

console output:

```bash
sbt:study-sbt> t1
What is your name?
Dennis
What is your age?
42
[info] Hello 'Dennis', you are '42' years old!
```

### Scopes
Key -> Value pairs play an important role in Sbt as they let us define settings and settings let us configure our projects and a build is made up out of one or more projects. Keys can easily be configured so that they have a value in a specific Configuration, Task or (Configuration,Task) combination. 

Sbt gives us shorthands so easily scope Keys. There are two Scopes ['Global'](http://www.scala-sbt.org/1.x/docs/Scopes.html#Global+scope+component) and ['ThisBuild'](http://www.scala-sbt.org/1.x/docs/Scopes.html#Build-level+settings). 

### ThisBuild Scope
Lets first task about ['ThisBuild'](http://www.scala-sbt.org/1.x/docs/Scopes.html#Build-level+settings). When a Build consists of *multiple projects*, then the Scope 'ThisBuild' is handy. For a single project 'build.sbt', the Scope 'ThisBuild' doesn't make much sense, as the configuration will apply for the single (default) project.

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
[info] 2.12.4
```

Until now we haven't seen this syntax. The project name is also part of a Key. So the fully qualified name of a key is really:

```bash
(project/config:task::setting)
```

Of course we can leave parts of like so:

```bash
> project1/scalaVersion
[info] 2.12.4
> project1/test:scalaVersion
[info] 2.12.4
> project1/test:test::scalaVersion
[info] 2.12.4
```

Now lets say that we want to configure the scalaVersion only for 'project1' then we would type:

```
set scalaVersion in project1 := "2.11.8"
[info] Reapplying settings...
> project1/scalaVersion
[info] 2.11.8

> project2/scalaVersion
[info] 2.12.4
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

Now say that we want to set the scalaVersion for all projects in our *Build*, then we would configure:

```bash
> set scalaVersion in ThisBuild := "2.11.8"
[info] Reapplying settings...

> project1/scalaVersion
[info] 2.12.1

> project2/scalaVersion
[info] 2.11.8
```

Because we haven't removed the specific configuration that we have set on 'project1', that scalaVersion is still '2.12.1' but the setting for 'project2' has changed. The scope ['ThisBuild'](http://www.scala-sbt.org/1.x/docs/Scopes.html#Build-level+settings) is shorthand for the following definition:

```
All projects and all configuration and all tasks in the current build only.
```

### Global Scope
The scope ['Global'](http://www.scala-sbt.org/1.x/docs/Scopes.html#Global+scope+component) is handy to define settings that apply to all projects everywhere on your computer or your enterprise, and all of there configurations and all of there tasks. I guess that covers 'Global'. This scope only makes sense if you create plugins and you want to add the setting to all projects everywhere. If you can remember that Keys are scoped on [Axis](http://www.scala-sbt.org/1.x/docs/Scopes.html#Scope+axes), so (Project/Configuration:Task) then the difference between the scope 'ThisBuild' and 'Global' is that for 'ThisBuild' the axis looks like ({.}/*:*) and for Global the axis looks like (*/*:*).

## Parsing user input
SBT supports [parsing user input](http://www.scala-sbt.org/1.x/docs/Parsing-Input.html) as part of a task. To parse use input we use the `inputKey` eg:

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
The [sourceGenerators](https://github.com/sbt/sbt/blob/1.0.x/main/src/main/scala/sbt/Keys.scala#L180) setting defines a list of
tasks that generate sources. A source generation task should generate sources in a subdirectory of `sourceManaged` and return a sequence of files generated.

The key to add the task to is called `sourceGenerators`. Because we want to add the task, and not the value after its execution we use `taskValue` instead of the usual `value`. It should be scoped according to whether the generated files are main (Compile) or test (Test) sources.

For example, lets say we want to generate an `BuildInfo.scala` file that contains information about our build. We can do the following:

- create a task 'getBuildInfo' that aggregates information about our build,
- create a task 'makmakeBuildInfo' that will create the 'Information.scala' file in the sourceManaged dir and stores the
  information that has been aggregated by 'getBuildInfo' into that file
- create a 'main.Main' console application that uses the 'BuildInfo.scala' file
- run the console application

```scala
lazy val getCommitSha = taskKey[String]("Returns the current git commit SHA")

getCommitSha := {
  Process("git rev-parse HEAD").lines.head
}

lazy val getCurrentDate = taskKey[String]("Get current date")

getCurrentDate := {
  new java.text.SimpleDateFormat("yyyy-HH-mm'T'hh:MM:ss.SSSSXX").format(new java.util.Date())
}

lazy val getBuildInfo = taskKey[String]("Get information about the build")

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

lazy val makeBuildInfo = taskKey[Seq[File]]("Makes the BuildInfo.scala file")

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
lazy val gitCommitSha = taskKey[String]("Returns the current git commit SHA")

gitCommitSha := {
  Process("git rev-parse HEAD").lines.head
}

// 'makeVersionConfig' will create the 'version.config' file
lazy val makeVersionConfig = taskKey[Seq[File]]("Makes a version config file")

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

## Server, Client, Shell
[Sbt server](http://www.scala-sbt.org/1.x-beta/docs/sbt-server.html) is a feature of sbt 1.x that adds network access to a single running instance of Sbt. This allows multiple clients
to connect to a single session of Sbt. The primary use case is tooling integration such as editors and IDEs.

There are three new commands:
- *sbt shell*: provides an interactive prompt from which commands can be run
- *sbt client 127.0.0.1:<port>*: provides an interactive prompt for which commands can be run on a server
- *sbt startServer*: starts the sbt server if it has not been started.

By default, sbt interactive mode is started when no commands are provided on the CLI. The interactive shell is also started when the `shell` command is invoked with the
command `sbt shell`. The `sbt shell` command does not only launch an interactive shell, but it also launches the sbt server. When you just type `sbt -Dsbt.server.autostart=false`,
the server is not started but the interactive shell.

When you start an sbt session by typing `sbt`, the interactive shell and sbt server is started. The server will output the port its running on:

```bash
$ sbt
[info] Loading settings from idea.sbt,sbt-updates.sbt ...
[info] Loading global plugins from /Users/dennis/.sbt/1.0/plugins
[info] Loading settings from plugins.sbt ...
[info] Loading project definition from /Users/dennis/projects/study-sbt/project
[info] Loading settings from build.sbt ...
[info] Set current project to study-sbt (in build file:/Users/dennis/projects/study-sbt/)
[info] sbt server started at 127.0.0.1:5829
```

The server is running on '127.0.0.1:5829'.

When we start a new terminal session and from an arbitrary directory location on your system type: `sbt client 127.0.0.1:5829`, we get the following output on the server:

```bash
sbt:study-sbt> [info] new client connected from: 50745
```

and we get the following output on the client:

```bash
$ sbt client 127.0.0.1:5829
[info] Loading settings from idea.sbt,sbt-updates.sbt ...
[info] Loading global plugins from /Users/dennis/.sbt/1.0/plugins
[info] Updating {file:/Users/dennis/.sbt/1.0/plugins/}global-plugins...
[info] Done updating.
[info] Loading project definition from /Users/dennis/projects/project
[info] Updating {file:/Users/dennis/projects/project/}projects-build...
[info] Done updating.
[info] Set current project to projects (in build file:/Users/dennis/projects/)
client on port 5829
ChannelAcceptedEvent(channel-1)
```

We can now type commands in the client, but please note that input like 'show name' (the first thing I did) only shows up on the server as output.

The command `exit` closes the connection as expected.

### Forking processes
Sbt can fork processes with the [Fork API](http://www.scala-sbt.org/1.x/docs/Forking.html). Lets fork the following  application which is in src/main/scala/main/Main.scala:

```scala
package main

object Main extends App {
  println("Hello World!: " + args.toList)
}
```

Now lets look at our build:

```scala
val task1 = taskKey[Unit]("")
task1 := {
  def classpathOption(classpath: Seq[File]): Seq[String] = {
    "-classpath" :: Path.makeString(classpath) :: Nil
  }
  val cp: Seq[File] = (fullClasspath in Compile).value.map(_.data)
  val mainClass: String = "main.Main"
  val arguments: Seq[String] = Seq("Hello World!")
  val outputStrategy = StdoutOutput
//  val outputStrategy = LoggedOutput(streams.value.log)
  val config: ForkOptions = ForkOptions(
    javaHome.value,
    Some(outputStrategy), // StdOutput, LoggedOutput
    Vector(),
    Some(baseDirectory.value),
    javaOptions.value.toVector,
    connectInput.value,
    envVars.value
  )
  val fullArguments: Seq[String] = classpathOption(cp) ++ Seq(mainClass) ++ arguments

  // Process is being executed
  val exitValue = Fork.java(config, fullArguments)
}
```

Fork just calls the 'java' command just like you would on the CLI. As you know, you can put all kinds of arguments to make java work and a full command would be something like:

```bash
java -classpath <all jars here> main.Main <args here>
```

Sbt has abstracted such calls away for us and exposes it as the Fork API. We have to do some setup to make it work like:

- Determine what the main class is (main.Main)
- Determine what the arguments for the main class is (arguments)
- Determine what the classpath is (the part after -classpath) and must be placed before 'main.Main <arguments>',
- Determine how to log the output of our forked process (to StdOutput or the Sbt Logger)
- Determine javaHome, boot classes (Vector()), the baseDir to use, the javaOptions and environment variables

In short, we are setting up a fully configured JVM instance and everything must be just right to launch our 'main.Main' class.

Finally, we can make the call to `Fork.java(config, fullArguments)` and it is a blocking call to that JVM. The result is an exitValue of type `Int`.

The dependency of this task is the following and as expected we see all tasks and settings we call before we can Fork our process:

```bash
sbt:study-sbt> inspect task1
[info] Task: Unit
[info] Description:
[info]
[info] Provided by:
[info] 	{file:/Users/dennis/projects/study-sbt/}study-sbt/*:task1
[info] Defined at:
[info] 	/Users/dennis/projects/study-sbt/build.sbt:2
[info] Dependencies:
[info] 	*:connectInput
[info] 	*:envVars
[info] 	*:javaHome
[info] 	*:javaOptions
[info] 	compile:fullClasspath
[info] 	*:baseDirectory
[info] Delegates:
[info] 	*:task1
[info] 	{.}/*:task1
[info] 	*/*:task1
```

### Using a ScalaRun 'runner'
The previous example when we forked a program can become much shorter when we use the `Keys.runner` which is implemented in `Defaults.runnerTask` and returns a `ScalaRun`, 'a fully configured runner implementation used to run a main class' like the `main.Main` class for example. The task will become much shorter:

```scala
 val runnerToUse: ScalaRun = runner.value
  Run.run(
  "main.Main",
  (fullClasspath in Compile).value.map(_.data),
  Seq("hello", "world"),
  streams.value.log)(runnerToUse)
```

Note that we can put the runner in implicit scope if we wish because it is just a generic runner that can be injected anywhere we like. Here I'm passing it explicitly to `Run.run(...)(runnerToUse)` to point out the use case.

If we look at the dependencies then we see the following:

```bash
sbt:study-sbt> inspect task1
[info] Task: Unit
[info] Description:
[info]
[info] Provided by:
[info] 	{file:/Users/dennis/projects/study-sbt/}study-sbt/*:task1
[info] Defined at:
[info] 	/Users/dennis/projects/study-sbt/build.sbt:2
[info] Dependencies:
[info] 	*:task1::streams
[info] 	compile:fullClasspath
[info] 	*:runner
[info] Delegates:
[info] 	*:task1
[info] 	{.}/*:task1
[info] 	*/*:task1
```

### Compiling a file in a task
The following example shows how to create a task that will create a file, compile the file and runs that file. Please note that is contains some code, and most of the code are really just two 'no-operation' classes necessary for the compiler.

The example shows how to create a task, that task sets up some resources that we need. Good practice is to get all the sbt values at the top of your code and then, when all calls to `.value` are done, begin with your logic. This way the flow of execution works as expected.

Compiling files is not trivial, even when using Sbt's abstraction and APIs. Still, in just 100 lines we create a file, compile it and run it which is not bad.

```scala
val task1 = taskKey[Unit]("")

task1 := {
    val compilers = Keys.compilers.value
    val classpath: Seq[File] = (fullClasspath in Compile).value.map(_.data)
    val outputDir: File = (classDirectory in Compile).value
    val options: Seq[String] = (scalacOptions in Compile).value
    val inputs: xsbti.compile.Inputs = (compileInputs in Compile in compile).value
    val cache: xsbti.compile.GlobalsCache = inputs.setup().cache()
    val log = streams.value.log
    implicit val runnerToUse: ScalaRun = runner.value

    val targetDir: File = target.value
    val fileToCompile: File = targetDir / "Engage.scala"
    val maxErrors: Int = 1000

    // create the Engage.scala file
    IO.createDirectory(targetDir)
    IO.write(fileToCompile, """object Engage extends App { println("Engage!") }""")
    // compile the file

    compilers.scalac() match {
      case compiler: sbt.internal.inc.AnalyzingCompiler =>
        compileSingleFile(
          compiler,
          fileToCompile,
          classpath,
          outputDir,
          options,
          maxErrors,
          cache,
          noChanges,
          noopCallback,
          log
        )
      case _ => sys.error("Expected a 'sbt.internal.inc.AnalyzingCompiler' compiler")
    }

  // lets run 'Engage'
  runSingleFile("Engage", classpath, Seq.empty, log)
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

def compileSingleFile(
                       compiler: sbt.internal.inc.AnalyzingCompiler,
                       fileToCompile: File,
                       classpath: Seq[File],
                       outputDir: File,
                       options: Seq[String],
                       maxErrors: Int,
                       cache: xsbti.compile.GlobalsCache,
                       dependencyChanges: xsbti.compile.DependencyChanges,
                       analysisCallback: xsbti.AnalysisCallback,
                       log: sbt.internal.util.ManagedLogger): Unit = {

  log.info(s"Compiling a single file: $fileToCompile")

      compiler.apply(
        Array(fileToCompile),
        dependencyChanges,
        classpath.toArray,
        outputDir,
        options.toArray,
        analysisCallback,
        maxErrors,
        cache,
        log
      )
}

lazy val noChanges = new xsbti.compile.DependencyChanges {
  def isEmpty = true
  def modifiedBinaries = Array()
  def modifiedClasses = Array()
}

lazy val noopCallback = new xsbti.AnalysisCallback {
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
```

### Getting Types from the Scala Type System by String
It is possible, but not encouraged, to get types from the scala Type System by String. Normally we would do something like the following to get a reference to `List[Int]`:

```bash
scala> import scala.reflect.runtime.universe._
import scala.reflect.runtime.universe._

scala> typeOf[List[Int]]
res0: reflect.runtime.universe.Type = scala.List[Int]
```

We could also 'ask the compiler' to create a `TypeTag` for us and use the `tpe` method to get to the type:

```bash
scala> implicitly[TypeTag[List[Int]]]
res1: reflect.runtime.universe.TypeTag[List[Int]] = TypeTag[scala.List[Int]]

scala> res1.tpe
res2: reflect.runtime.universe.Type = scala.List[Int]
```

The downside of this approach is that we must give the type as a literal like `List[Int]`, and when working with SBT, this is not always possible.

When we break the problem down a bit, would it be possible to construct a TypeTag and give it two pieces of information? For example, I want to get a TypeTag for a `scala.collection.immutable.List` that must be constructed with a `scala.Int`, shouldn't be that hard, or is it?

Lets create the following object in `project/CustomLoader.scala`. We will import some packages from `scala.reflect` that interfers with the `build.sbt` default imports:

```scala
object CustomLoader {
  import scala.reflect.api
  import scala.reflect.api.{TypeCreator, Universe}
  import scala.reflect.runtime.universe._

  def createTypeTagForST(simpleTypeName: String, classLoader: Option[ClassLoader] = None): TypeTag[_] = {
    val currentMirror = classLoader
      .map(cl => scala.reflect.runtime.universe.runtimeMirror(cl))
      .getOrElse(scala.reflect.runtime.currentMirror)
    val typSym = currentMirror.staticClass(simpleTypeName)
    val tpe = internal.typeRef(NoPrefix, typSym, List.empty)
    val ttag = TypeTag(currentMirror, new TypeCreator {
      override def apply[U <: Universe with Singleton](m: api.Mirror[U]): U#Type = {
        assert(m == currentMirror, s"TypeTag[$tpe] defined in $currentMirror cannot be migrated to $m.")
        tpe.asInstanceOf[U#Type]
      }
    })
    ttag
  }

  def createTypeTagForHKT(higherKindedTypeName: String = "scala.collection.immutable.List",
                                    parameterSymbol: String = "scala.Int",
                                    classLoader: Option[ClassLoader] = None): TypeTag[_] = {
    val currentMirror = classLoader
       .map(cl => scala.reflect.runtime.universe.runtimeMirror(cl))
      .getOrElse(scala.reflect.runtime.currentMirror)
    val typSym = currentMirror.staticClass(higherKindedTypeName)
    val paramSym = currentMirror.staticClass(parameterSymbol)
    val tpe = internal.typeRef(NoPrefix, typSym, List(paramSym.selfType))
    val ttag = TypeTag(currentMirror, new TypeCreator {
      override def apply[U <: Universe with Singleton](m: api.Mirror[U]): U#Type = {
        assert(m == currentMirror, s"TypeTag[$tpe] defined in $currentMirror cannot be migrated to $m.")
        tpe.asInstanceOf[U#Type]
      }
    })
    ttag
  }
}
```

We have two methods in our 'CustomLoader', one method that creates a TypeTag for a SimpleType like 'scala.Int' and another that creates TypeTags for higher kinded types like a `List[Int]`. The only price we pay is that we loose typing information so the return type is a TypeTag of anything so `TypeTag[_]`

Lets use it. Back in our `build.sbt`, lets create a task that will create both a higher kinded type and a simple type:

```scala
lazy val task1 = taskKey[Unit]("Load simple types and higher kinded types")
task1 := {
  import scala.reflect.runtime.universe._
  val listOfIntTypeTag: TypeTag[_] = CustomLoader.createTypeTagForHKT("scala.collection.immutable.List", "scala.Int")
  assert(listOfIntTypeTag.toString == "TypeTag[List[Int]]")

  val intTypeTag: TypeTag[_] = CustomLoader.createTypeTagForST("scala.Int")
  assert(intTypeTag.toString == "TypeTag[Int]")
}
```

We can optionally provide a classloader that can load our custom types for example a `main.Person`. We must first have a task that can provide such a classloader on demand:

```scala
lazy val getFullClassLoader = taskKey[ClassLoader]("Returns a classloader that can load all project dependencies and compiled sources")
getFullClassLoader := {
  val scalaInstance: ScalaInstance = Keys.scalaInstance.value
  val fullClasspath: Seq[File] = (Keys.fullClasspath in Compile).value.map(_.data)
  val classDirectory: File = (Keys.classDirectory in Compile).value
  val classpath = Seq(classDirectory) ++ fullClasspath
  val cl: ClassLoader = sbt.internal.inc.classpath.ClasspathUtilities.makeLoader(classpath, scalaInstance)
  cl
}
```

This classloader can be used in the following task in where we will load a case class `main.Person` that is in our unmanaged source directory `src/main/scala/main/Person.scala`:

```scala
package main

import scala.annotation.StaticAnnotation

case class Named(name: String = "", age: String = "") extends StaticAnnotation

@Named(name = "Dennis", age = "43")
@MyMarker()
case class Person(name: String, age: Int)
```

The class has been annotated with both a Java and a Scala annotation:

```java
package main;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MyMarker {
    String name() default "N/A";
    String author() default "Dennis Was Here";
}
```

Lets get a `TypeTag` from it and query it for information. Please note that we are using the `getFullClassLoader` task and giving it to the `CustomLoader` so it can resolve all types:

```scala
lazy val task2 = taskKey[Unit]("Load custom types and annotations")
task2 := {
  import scala.reflect.runtime.universe._
  val cl = getFullClassLoader.value
  val personTypeTag: TypeTag[_] = CustomLoader.createTypeTagForST("main.Person", Option(cl))
  assert(personTypeTag.toString == "TypeTag[Person]")
  assert(personTypeTag.tpe.typeSymbol.annotations.mkString(",") == """main.Named("Dennis", "43"),main.MyMarker""")

  val namedAnnotationTypeTag: TypeTag[_] = CustomLoader.createTypeTagForST("main.Named", Option(cl))
  assert(namedAnnotationTypeTag.toString == "TypeTag[Named]")

  val maybeNamedAnnotation: Option[Annotation] = personTypeTag.tpe.typeSymbol.annotations.find(_.tree.tpe =:= namedAnnotationTypeTag.tpe)

  assert(maybeNamedAnnotation.isDefined)
}
```

### Settings Initialization
You know by now that an Sbt build consist of one or more project and those projects consists of settings and tasks. The best description on how this process works is described in the [Sbt documentation - Settings Initialization](http://www.scala-sbt.org/1.0/docs/Setting-Initialization.html) which is a 10 minute read. 

The process of initialization consists of two steps:

1. Collect all settings from defined locations
2. Apply all settings in a predetermined order

### 1. Collecting settings
Settings are collected from predefined locations:

- User-level project (~/.sbt/<version>):
  - Load any plugins defined in `~/.sbt/<version>/plugins/*.sbt`
  - Load any plugins defined in `~/.sbt/<version>/plugins/*.scala`
  - Load all settings defined in `~/.sbt/<version>/*.sbt`
  - Load all settings defined in `~/.sbt/<version>/*.scala)`
- SBT Project-level:
  - Load any plugins defined in `project/plugins.sbt`
  - Load any plugins defined in `project/project/*.scala`
  - Load any settings defined in `project/*.sbt`
  - Load any settings defined in `project/*.scala`
  - Load project `*.sbt` files `build.sbt` and friends.

The result of all this resolving is a sequence of `Seq[Setting[_]]` that must be ordered and thus there are settings that will override other settings.

### 2. Apply settings in a predefined order
The settings collected in the previous step, which is a `Seq[Setting[_]]` must be ordered. There is a predefined way to order these settings and therefor settings will be override other settings. The sequence is:

1. All AutoPlugin settings
2. All settings defined in `project/Build.scala`
3. All settings defined in the user directory `~/.sbt/<version>/*.sbt`
4. All local configurations `build.sbt` 

The effectiv result is a task graph that is used to execute the build.

### Commands
There are several definition of 'a command' when you are working with SBT. When working with Sbt, the things you type into the console are called 'commands'. These commands-you-type most often trigger a 'task' or a 'setting' like for example 'name' that will evaluate the setting 'name'. Besides a 'setting' or a 'task' there is a third thing that can be executed and that thing is called a 'command'.

There is a technical distinction in sbt between tasks, which are 'inside' the build definition, and commands, which manipulate the build definition itself. Most often it is not necessary to create commands because most activities can be implemented by chaining multiple tasks.

You would need a task if you need to manipulate the state of the build.


Lets create an helloworld build:

```scala
lazy val hello = Command.command("hello") { state =>
  println("Hello there!")
  state
}

commands += hello
```

The previous creates a command and stores it into the value 'hello' and adds the command to the list of commands of Sbt.

We can now execute the command by typing `hello`:

```bash
sbt:study-sbt> hello
Hello there!
```

The point of working with Commands is to 'get to the state of the project', so we need to extract information from it:

```scala
lazy val hello = Command.command("hello") { state =>
  val extracted = Project.extract(state)
  import extracted._
  println("Current build: " + currentRef.build)
  println("Current project: " + currentRef.project)
  println("Original setting count: " + session.original.size)
  println("Session setting count: " + session.append.size)

  state
}

commands += hello
```

We can now take a look at the extracted project state:

```bash
sbt:study-sbt> hello
Current build: file:/Users/dennis/projects/study-sbt/
Current project: study-sbt
Original setting count: 667
Session setting count: 0
```

### SBT types to know
When studying sbt, it is handy to take a look at some parts of its codebase like:

- [(sbt-main) - sbt.Keys](https://github.com/sbt/sbt/blob/1.x/main/src/main/scala/sbt/Keys.scala): Defines all the available keys. This is a great place to start as all keys have a textual description to read what the keys do and also a quick way to search for keywords if you're looking for some functionality.
- [(sbt-main) - sbt.Defaults](https://github.com/sbt/sbt/blob/1.x/main/src/main/scala/sbt/Defaults.scala): All the default implementation and settings for the available Keys. This is a great place to go look for how a task is wired.
- [(sbt-main) - sbt.Project](https://github.com/sbt/sbt/blob/1.x/main/src/main/scala/sbt/Project.scala): This class defines an sbt project. So if you type `project` in an sbt-session or type `lazy val myproj = project in file(".")` or just want a reference to the project in a build.sbt and type `project` somewhere in your build.sbt, you get a reference to this type.
- [(sbt-io) - sbt.io.IO](https://github.com/sbt/io/blob/1.x/io/src/main/scala/sbt/io/IO.scala): The io library is a great place to get ideas to use with your own API. For starters it has some really nice implicit conversion ideas to make working with files and direrectories easy. `IO.write` and `IO.createDirectory` are a great place to start looking at the library.

## intellij sbt plugin
- [Scala plugin for IntelliJ IDEA 2017.1](https://blog.jetbrains.com/scala/2017/03/23/scala-plugin-for-intellij-idea-2017-1-cleaner-ui-sbt-shell-repl-worksheet-akka-support-and-more/)
- [Scala plugin Bugs](https://intellij-support.jetbrains.com/hc/en-us/community/topics/200381545-Scala)

Have fun!