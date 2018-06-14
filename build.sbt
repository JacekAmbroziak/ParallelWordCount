name := "WordCount"

version := "0.1"

scalaVersion := "2.12.6"

target := file("/tmp/sbt") / name.value

testNGVersion := "6.11"

// https://mvnrepository.com/artifact/com.google.guava/guava
libraryDependencies += "com.google.guava" % "guava" % "25.1-jre"

// https://mvnrepository.com/artifact/org.testng/testng
libraryDependencies += "org.testng" % "testng" % "6.11" % Test

// https://mvnrepository.com/artifact/org.scalatest/scalatest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0-SNAP10" % Test

// https://mvnrepository.com/artifact/org.scalacheck/scalacheck
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test

enablePlugins(TestNGPlugin)
