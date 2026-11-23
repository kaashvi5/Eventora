ThisBuild / scalaVersion := "3.3.6"
ThisBuild / version := "2.0.0"
ThisBuild / organization := "com.eventticketing"

lazy val root = (project in file("."))
  .settings(
    name := "eventora-ticketing-system",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.2" % Test
  )
