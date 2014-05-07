import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys

object MyBuild extends Build {
  val Organization = "machisoccer"
  val Name = "webtest"
  val Version = "0.0.1"
  val ScalaVersion = "2.10.3"

  lazy val project = Project (
    "webtest",
    file("."),
    settings = Defaults.defaultSettings ++ Seq(
      sourcesInBase := false,
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers ++= Seq(
        Classpaths.typesafeReleases,
        "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"
      ),
      scalacOptions := Seq("-deprecation", "-language:postfixOps"),
      libraryDependencies ++= Seq(
        "com.github.nscala-time" %% "nscala-time" % "1.0.0",
        "ch.qos.logback" % "logback-classic" % "1.1.2",
        "com.typesafe" %% "scalalogging-slf4j" % "1.1.0",
        "org.scalatest" %% "scalatest" % "2.1.3",
        "org.seleniumhq.selenium" % "selenium-java" % "2.41.0",
        "com.google.code.findbugs" % "jsr305" % "2.0.3"
      ),
      EclipseKeys.withSource := true,
      javacOptions in compile ++= Seq("-target", "6", "-source", "6"),
      testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/report")
      //packageOptions += Package.MainClass("JettyLauncher")
    )
  )
}
