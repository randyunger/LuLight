import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._
//import sbtassembly.AssemblyKeys._
//import sbtassembly.AssemblyPlugin.autoImport._

object LuLightBuild extends Build {
  val Name = "LuLight"
  //  val Version = "0.1.1"
  val ScalaVersion = "2.11.6"
  val ScalatraVersion = "2.4.0-RC2-2"
  val jettyVersion = "9.3.5.v20151012"

  lazy val commonSettings = Seq(
    version := "0.1.1",
    organization := "org.runger.lulight",
    scalaVersion := ScalaVersion
  )

  resolvers += Resolver.jcenterRepo
  resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"
  //  enablePlugins(TomcatPlugin)

  lazy val app = (project in file("app")).
    settings(commonSettings: _*).
    settings(
//      assemblyJarName in assembly := "LuLight.jar",
//      test in assembly := {},
//      mainClass in assembly := Some("org.runger.")
    )

  lazy val project = Project (
    "LuLight",
    file("."),
    settings = ScalatraPlugin.scalatraSettings ++ scalateSettings ++ commonSettings ++ Seq(
      //      organization := Organization,
      name := Name,
      //      version := Version,
      //      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-library" % ScalaVersion
        ,"org.scala-lang" % "scala-reflect" % ScalaVersion
        ,"org.scala-lang" % "scala-compiler" % ScalaVersion
        ,"org.scalatra" %% "scalatra" % ScalatraVersion
        ,"org.scalatra" %% "scalatra-scalate" % ScalatraVersion
        ,"org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test"
        ,"org.eclipse.jetty" % "jetty-util" % jettyVersion
        ,"org.eclipse.jetty" % "jetty-webapp" % jettyVersion
        ,"org.eclipse.jetty" % "jetty-server" % jettyVersion
        ,"org.eclipse.jetty" % "jetty-servlet" % jettyVersion
        ,"ch.qos.logback" % "logback-classic" % "1.1.2"
        ,"javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
        ,"commons-net" % "commons-net" % "2.0"
        ,"com.typesafe.akka" % "akka-actor_2.11" % "2.3.11"
        ,"com.typesafe.akka" % "akka-testkit_2.11" % "2.3.11" % "test"
        ,"org.scala-lang.modules" %% "scala-xml" % "1.0.3"
        ,"com.typesafe.play" % "play-json_2.11" % "2.5.0-M1"
        ,"org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  )
}
