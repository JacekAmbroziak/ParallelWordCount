name := "WordCount"

version := "0.1"

scalaVersion := "2.12.6"

target := file("/tmp/sbt") / name.value

// https://mvnrepository.com/artifact/com.google.guava/guava
libraryDependencies += "com.google.guava" % "guava" % "25.1-jre"

// https://mvnrepository.com/artifact/org.testng/testng
libraryDependencies += "org.testng" % "testng" % "6.14.3" % Test
