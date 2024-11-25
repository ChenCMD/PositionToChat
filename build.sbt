name := "PositionToChat"

version := "1.0.0"

scalaVersion := "3.6.2-RC1"

resolvers ++= Seq(
  "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots",
  "maven.elmakers.com" at "https://maven.elmakers.com/repository/",
  Resolver.jcenterRepo,
  Resolver.bintrayIvyRepo("com.eed3si9n", "sbt-plugins")
)

libraryDependencies ++= Seq(
  "org.spigotmc"            % "spigot-api"               % "1.20.4-R0.1-SNAPSHOT",
  "org.typelevel"          %% "cats-effect"              % "3.4.8",
  "org.typelevel"          %% "log4cats-slf4j"           % "2.7.0",
  "com.github.tarao"       %% "record4s"                 % "0.13.0",
)

unmanagedBase := baseDirectory.value / "localDependencies"

excludeDependencies := Seq(
  ExclusionRule(organization = "org.bukkit", name = "bukkit")
)

semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision

usePipelining                    := true
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xkind-projector:underscores",
  "-no-indent",
  "-Wunused:all",
  "-source:future"
)

assembly / assemblyExcludedJars  := {
  (assembly / fullClasspath).value.filter(
    _.data.absolutePath.startsWith((baseDirectory.value / "localDependencies").absolutePath)
  )
}

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*)                                              => MergeStrategy.discard
  case "module-info.class"                                                        => MergeStrategy.first
  case x => (ThisBuild / assemblyMergeStrategy).value(x)
}

assembly / assemblyOutputPath    := new File(baseDirectory.value, "dist/PositionToChat.jar")
