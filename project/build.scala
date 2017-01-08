import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._
import com.gilt.aws.lambda.{AwsLambdaPlugin, _}
//import sbtassembly.AssemblyKeys._
//import sbtassembly.AssemblyPlugin.autoImport._

object LuLightBuild extends Build {
  val Name = "LuLight"
  val ScalaVersion = "2.11.8"
  val ScalatraVersion = "2.4.0-RC2-2"
  val jettyVersion = "9.3.5.v20151012"

  lazy val commonSettings = Seq(
    version := "0.2.3",
    organization := "org.runger.lulight",
    scalaVersion := ScalaVersion
  )

  import AwsLambdaPlugin.autoImport._
  lazy val lambdaSettings = Seq(

    AwsLambdaPlugin.autoImport.lambdaHandlers := Seq(
    "LuLight"                 -> "org.runger.lulight.LambdaHandler::handleRequest"
//    "function2"                 -> "com.gilt.example.Lambda::handleRequest2",
//    "function3"                 -> "com.gilt.example.OtherLambda::handleRequest3"
    )

    // or, instead of the above, for just one function/handler
    //
    // lambdaName := Some("function1")
    //
    // handlerName := Some("com.gilt.example.Lambda::handleRequest1")

    , s3Bucket := Some("lulight-lambda-jars")

    , awsLambdaMemory := Some(192)

    , awsLambdaTimeout := Some(30)

    , roleArn := Some("arn:aws:iam::089420071793:role/lambda_basic_execution")
  )

  //  enablePlugins(TomcatPlugin)

  lazy val project = Project (
    "LuLight",
    file("."),
    settings = lambdaSettings ++ ScalatraPlugin.scalatraSettings ++ scalateSettings ++ commonSettings ++ Seq(
      //      organization := Organization,
      name := Name,
      //      version := Version,
      //      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      resolvers += Resolver.jcenterRepo,
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      resolvers += "anormcypher" at "http://repo.anormcypher.org/",
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
        , "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"
        ,"io.reactivex" %% "rxscala" % "0.26.0"
        ,"ch.qos.logback" % "logback-classic" % "1.1.2"
        ,"javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
        ,"commons-net" % "commons-net" % "2.0"
        ,"com.typesafe.akka" % "akka-actor_2.11" % "2.4.2"
        ,"com.typesafe.akka" % "akka-testkit_2.11" % "2.4.2" % "test"
        ,"org.scala-lang.modules" %% "scala-xml" % "1.0.3"
        ,"com.typesafe.play" % "play-json_2.11" % "2.5.0-M1"
        ,"org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"
        ,"com.amazonaws" % "aws-lambda-java-core" % "1.1.0"
        ,"com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0"
//        ,"com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.2"
        ,"com.typesafe.slick" %% "slick" % "3.1.1"
        ,"com.typesafe.slick" %% "slick-hikaricp" % "3.1.1"
        ,"com.h2database" % "h2" % "1.4.191"
//        ,"postgresql" % "postgresql" % "9.1-901.jdbc4"
        ,"org.postgresql" % "postgresql" % "9.4.1211"
        ,"org.anormcypher" %% "anormcypher" % "0.9.1"
        ,"org.neo4j.driver" % "neo4j-java-driver" % "1.0.5"
        ,"org.neo4j" % "neo4j" % "3.0.6"

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
  ).enablePlugins(AwsLambdaPlugin)
}
