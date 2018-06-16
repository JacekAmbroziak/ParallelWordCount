name := "WordCount"

version := "0.2"

scalaVersion := "2.12.6"

target := file("/tmp/sbt") / name.value

// https://mvnrepository.com/artifact/com.google.guava/guava
libraryDependencies += "com.google.guava" % "guava" % "25.1-jre"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0-SNAP10" % Test
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
libraryDependencies += "junit" % "junit" % "4.12" % Test
crossPaths := false
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test


testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a"))

//libraryDependencies ++= Seq(
  //  "junit" % "junit" % "4.12" % Test,
  //  "com.novocode" % "junit-interface" % "0.11" % Test exclude("junit", "junit-dep")
  //  crossPaths := false,
  //  "com.novocode" % "junit-interface" % "0.11" % Test

  // https://mvnrepository.com/artifact/org.scalatest/scalatest
//  "org.scalatest" %% "scalatest" % "3.2.0-SNAP10" % Test,
//  "junit" % "junit" % "4.12" % Test,
//  crossPaths := false,
//  "com.novocode" % "junit-interface" % "0.11" % Test
//)
