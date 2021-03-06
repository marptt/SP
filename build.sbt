name := "SequencePlanner"
scalaVersion := "2.11.7"

lazy val akka = Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.1",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.7"
)

lazy val json = Seq(
  "com.github.nscala-time" %% "nscala-time" % "2.0.0",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.json4s" %% "json4s-ext" % "3.2.11",
  "org.json4s" %% "json4s-jackson" % "3.2.11"
)

lazy val commonSettings = Seq(
  version := "0.6.0-SNAPSHOT",
  scalaVersion := "2.11.7",
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/Releases",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"),
  scalacOptions  := Seq(
    "-encoding", "utf8",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-target:jvm-1.8",
    "-language:implicitConversions",
    "-language:postfixOps"
  )
)





lazy val root = project.in( file(".") )
   .aggregate(core, domain, gui, extensions, launch)

lazy val domain = project.
  settings(commonSettings: _*).
  settings(libraryDependencies ++= json)

lazy val core = project.dependsOn(domain).
  settings(commonSettings: _*).
  settings(libraryDependencies ++= akka ++ json)

lazy val gui = project.dependsOn(domain, core).
  settings(commonSettings: _*).
  settings(libraryDependencies ++= akka ++ json)

lazy val extensions = project.dependsOn(domain, core).
  settings(commonSettings: _*).
  settings(libraryDependencies ++= akka ++ json)

lazy val launch = project.dependsOn(domain, core, gui, extensions).
  settings(commonSettings: _*).
  settings(libraryDependencies ++= akka ++ json)



