import pl.project13.scala.sbt.JmhPlugin
import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import xerial.sbt.Pack._

object Build extends Build {
  def mappingContainsAnyPath(mapping: (File, String), paths: Seq[String]): Boolean = {
    paths.foldLeft(false)(_ || mapping._1.getPath.contains(_))
  }

  lazy val root = Project("SuProject", file("."))
    .aggregate(suCore, suToggle, suNetty, suThrift)
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)

  lazy val suCore = Project("su-core", file("su-core"))
    .settings(basicSettings: _*)
    .settings(formatSettings: _*)
    .settings(releaseSettings: _*)
    .settings(libraryDependencies ++= Dependencies.coreLibs ++ Dependencies.netty4Libs)
    .settings(unmanagedSourceDirectories in Test += baseDirectory.value / "multi-jvm/scala")
    .dependsOn(suToggle)

  lazy val suToggle = Project("su-toggle", file("su-toggle"))
    .settings(basicSettings: _*)
    .settings(formatSettings: _*)
    .settings(releaseSettings: _*)
    .settings(libraryDependencies ++= Dependencies.coreLibs ++ Dependencies.jacksonLibs)
    .settings(unmanagedSourceDirectories in Test += baseDirectory.value / "multi-jvm/scala")

  lazy val suThrift = Project("su-thrift", file("su-thrift"))
    .settings(basicSettings: _*)
    .settings(formatSettings: _*)
    .settings(releaseSettings: _*)
    .settings(libraryDependencies ++= Dependencies.netty4Libs ++ Dependencies.scroogeLibs)
    .settings(unmanagedSourceDirectories in Test += baseDirectory.value / "multi-jvm/scala")
    .dependsOn(suCore, suToggle, suNetty)

  lazy val suNetty = Project("su-netty", file("su-netty"))
    .settings(basicSettings: _*)
    .settings(formatSettings: _*)
    .settings(releaseSettings: _*)
    .settings(libraryDependencies ++= Dependencies.netty4Libs)
    .settings(unmanagedSourceDirectories in Test += baseDirectory.value / "multi-jvm/scala")
    .dependsOn(suCore, suToggle)

  lazy val noPublishing = Seq(
    publish := (),
    publishLocal := (),
    // required until these tickets are closed https://github.com/sbt/sbt-pgp/issues/42,
    // https://github.com/sbt/sbt-pgp/issues/36
    publishTo := None
  )

  lazy val basicSettings = Seq(
    organization := "com.ynet",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.1",
    crossScalaVersions := Seq("2.11.8", "2.12.1"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "UTF-8"),
    javacOptions ++= Seq("-encoding", "UTF-8"),
    resolvers ++= Seq("Spray repository" at "http://repo.spray.io/")
  )

  lazy val releaseSettings = Seq(
    publishTo := {
      val nexus = "http://192.168.65.120:8081/nexus/"
      if (version.value.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { (repo: MavenRepository) => false },
    pomExtra := (
      <url>https://github.com/mqshen/spray-socketio</url>
        <licenses>
          <license>
            <name>The Apache Software License, Version 2.0</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
        </scm>
        <developers>
          <developer>
            <id>mqshen</id>
            <name>miaoqi shen</name>
            <email>goldratio87@gmail.com</email>
          </developer>
        </developers>
      )
  )

  lazy val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test := formattingPreferences)

  import scalariform.formatter.preferences._
  def formattingPreferences =
    FormattingPreferences()
      .setPreference(RewriteArrowSymbols, false)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(IndentSpaces, 2)

}

object Dependencies {
  val branch = "master"//Process("git" :: "rev-parse" :: "--abbrev-ref" :: "HEAD" :: Nil).!!.trim
  val suffix = if (branch == "master") "" else "-SNAPSHOT"

  val libthriftVersion = "0.5.0-7"

  val utilVersion = "6.43.0"
  val jacksonVersion = "2.8.4"
  val netty4Version = "4.1.9.Final"

  val scroogeVersion = "4.16.0" + suffix

  val netty4Libs = Seq(
    "io.netty" % "netty-handler" % netty4Version, 
    "io.netty" % "netty-transport" % netty4Version, 
    "io.netty" % "netty-transport-native-epoll" % netty4Version classifier "linux-x86_64", 
    "io.netty" % "netty-handler-proxy" % netty4Version)

  val coreLibs = Seq(
    "com.twitter" %% "util-core" % utilVersion,
    "com.twitter" %% "util-logging" % utilVersion,
    "com.twitter" %% "util-hashing" % utilVersion,
    "com.twitter" %% "util-cache" % utilVersion,
    "com.twitter" %% "util-security" % utilVersion,
    "com.twitter" %% "util-tunable" % utilVersion,
    "com.twitter" %% "util-jvm" % utilVersion,
    "com.twitter" %% "util-codec" % utilVersion,
    "com.twitter" %% "util-stats" % utilVersion
  )

  val thriftLibs = Seq(
    "com.twitter" % "libthrift" % libthriftVersion intransitive(),
    "org.slf4j" % "slf4j-api" % "1.7.7" % "provided"
  )

  val scroogeLibs = thriftLibs ++ Seq(
    "com.twitter" %% "scrooge-core" % scroogeVersion)

  val jacksonLibs = Seq(
    "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion exclude("com.google.guava", "guava")
  )
}
