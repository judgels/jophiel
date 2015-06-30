import de.johoop.testngplugin.TestNGPlugin
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import sbt.Keys.aggregate
import sbt.Keys.doc
import sbt.Keys.javaOptions
import sbt.Keys.libraryDependencies
import sbt.Keys.name
import sbt.Keys.packageDoc
import sbt.Keys.parallelExecution
import sbt.Keys.publishArtifact
import sbt.Keys.scalaVersion
import sbt.Keys.sourceGenerators
import sbt.Keys.sources
import sbt.Keys.test
import sbt.Keys.testOptions
import sbt.Keys.version
import sbtbuildinfo.Plugin._

lazy val ITTest = config("integration") extend(Test)

lazy val jophiel = (project in file("."))
    .enablePlugins(PlayJava, SbtWeb)
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
    .dependsOn(jophielcommons)
    .aggregate(jophielcommons)
    .settings(
        name := "jophiel",
        version := IO.read(file("version.properties")).trim,
        scalaVersion := "2.11.1",
        libraryDependencies ++= Seq(
            "org.webjars" % "jquery-textcomplete" % "0.3.7",
            "com.typesafe.play" %% "play-mailer" % "2.4.0",
            "org.webjars" % "zxcvbn" % "1.0",
            "org.seleniumhq.selenium" % "selenium-java" % "2.46.0",
            "org.testng" % "testng" % "6.8.8",
            "org.yaml" % "snakeyaml" % "1.12",
            "de.johoop" %% "sbt-testng-interface" % "3.0.2",
            "com.novocode" % "junit-interface" % "0.11",
            "org.specs2" %% "specs2-core" % "2.4.14"
        )
    )
    .settings(TestNGPlugin.testNGSettings: _*)
    .settings(
        aggregate in test := false,
        aggregate in jacoco.cover := false,
        TestNGPlugin.testNGSuites := Seq("test/resources/testngUnit.xml")
  )
    .settings(jacoco.settings: _*)
    .settings(
        parallelExecution in jacoco.Config := false
    )
    .settings(
        LessKeys.compress := true,
        LessKeys.optimization := 3,
        LessKeys.verbose := true
    )
    .settings(
        publishArtifact in (Compile, packageDoc) := false,
        publishArtifact in packageDoc := false,
        sources in (Compile, doc) := Seq.empty
    )
    .settings(buildInfoSettings: _*)
    .settings(
        sourceGenerators in Compile <+= buildInfo,
        buildInfoKeys := Seq[BuildInfoKey](name, version),
        buildInfoPackage := "org.iatoki.judgels.jophiel"
    )
    .configs(ITTest)
    .settings(inConfig(ITTest)(Defaults.testTasks) : _*)
    .settings(
        javaOptions in ITTest ++= Seq("-Dconfig.resource=test.conf"),
        parallelExecution in ITTest := false,
        testOptions in ITTest := Seq(Tests.Argument(TestNGPlugin.TestNGFrameworkID, (("-d" +: ("target/scala-" + scalaVersion.value.substring(0, 4) + "/testng") +: TestNGPlugin.testNGParameters.value) ++ Seq("test/resources/testngIT.xml")):_*))
    )


lazy val jophielcommons = RootProject(file("../judgels-jophiel-commons"))
