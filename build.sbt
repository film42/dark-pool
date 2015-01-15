name := "dark-pool"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.8",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.8",
  "org.scalatest" %% "scalatest" % "2.1.6",
  "org.scalacheck" %% "scalacheck" % "1.11.5"
)

resolvers += "Secured Central Repository" at "https://repo1.maven.org/maven2"

resolvers += "Secured Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"