name := "Light"

version := "1.0"

scalaVersion := "2.11.6"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps", "-language:existentials", "-language:reflectiveCalls")

incOptions := incOptions.value.withNameHashing(true)

resolvers += "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.6"
  ,"org.scalatra" %% "scalatra" % "2.4.0-RC2-2"
  ,"org.scalatra" %% "scalatra-scalate" % "2.4.0-RC2-2"
  ,"commons-net" % "commons-net" % "2.0"
  ,"com.typesafe.akka" % "akka-actor_2.11" % "2.3.11"
  ,"com.typesafe.akka" % "akka-testkit_2.11" % "2.3.11" % "test"
  ,"org.scala-lang.modules" %% "scala-xml" % "1.0.3"
  ,"org.mortbay.jetty" % "jetty-util" % "6.1.26" % "compile"
  ,"org.mortbay.jetty" % "jetty" % "6.1.26" % "compile"
//  ,"org.eclipse.jetty" % "jetty-server" % "7.6.8.v20121106"
//  ,"org.eclipse.jetty" % "jetty-server" % "9.3.3.v20150827"
//  ,"org.eclipse.jetty" %% "jetty-webapp" % "8.1.8.v20121106" % "container"
//  ,"org.eclipse.jetty.orbit" %% "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
)
