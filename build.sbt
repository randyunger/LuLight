name := "Light"

version := "1.0"

scalaVersion := "2.11.6"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps", "-language:existentials", "-language:reflectiveCalls")

incOptions := incOptions.value.withNameHashing(true)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.6"
  ,"commons-net" % "commons-net" % "2.0"
  ,"com.typesafe.akka" % "akka-actor_2.11" % "2.3.11"
  ,"com.typesafe.akka" % "akka-testkit_2.11" % "2.3.11" % "test"
)