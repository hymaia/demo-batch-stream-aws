ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "batch",
    idePackagePrefix := Some("fr.hymaia"),
    Compile / run / mainClass := Some("fr.hymaia.Ingestion"),
    Compile / packageBin / mainClass := Some("fr.hymaia.Ingestion")
  )

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.33"
libraryDependencies += "software.amazon.awssdk" % "secretsmanager" % "2.23.5"
libraryDependencies += "org.json4s" %% "json4s-native" % "4.0.7"
libraryDependencies += "software.amazon.awssdk" % "s3" % "2.24.10"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.10"
