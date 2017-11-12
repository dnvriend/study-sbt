
import com.github.dnvriend.ops.AllOps
import sbt._
import sbt.complete.DefaultParsers._
import sbt.internal.util.complete.Parser
import sjsonnew.BasicJsonProtocol._

object SettingsPluginKeys {
  lazy val users = taskKey[Seq[String]]("Get list of users")
  lazy val userName = settingKey[String]("The user name")
  lazy val printUserName = taskKey[Unit]("Shows the selected user name")
}

object SettingsPlugin extends AutoPlugin with AllOps {
  override def trigger = allRequirements

  val autoImport = SettingsPluginKeys

  import autoImport._

  def selectUserParser(state: State): Parser[String] = {
    val maybeUsers = SessionVar.load(users in Global, state)
    val strings = maybeUsers.getOrElse(Nil)
    Space ~> StringBasic.examples(strings: _*)
  }
  val selectUserNameCmd = Command("selectUserName")(selectUserParser) { (state, user) =>
    println("Selected: " + user)
    Settings.saveSettings()
    state.put(userName.key, user)
  }

  val loadSettingsCmd = Command.command("loadSettings") { state =>
    Settings.loadSettings()
    state.put(userName.key, "selected")
  }

  object Settings {
    def saveSettings(): Unit = println("Saving settings...")
    def loadSettings(): Unit = println("Loading settings...")
  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    users := Seq("foo", "bar", "baz", "quz"),
    users := users.storeAs(users in Global).value,
    printUserName := {
      val buildState = Keys.state.value
      val log = Keys.streams.value.log
      val name = userName.?.value.getOrElse(buildState.get(userName.key))
      log.info("You selected: " + name)
    },
    Keys.commands += loadSettingsCmd,
    Keys.commands += selectUserNameCmd,
  )
}
