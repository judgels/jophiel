import de.johoop.testngplugin.TestNGPlugin
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
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
        scalaVersion := "2.11.7",
        libraryDependencies ++= Seq(
            "com.typesafe.play" %% "play-mailer" % "3.0.1",
            "org.webjars" % "jquery-textcomplete" % "0.3.7",
            "org.webjars" % "zxcvbn" % "1.0"
        ),
        routesGenerator := InjectedRoutesGenerator,
        PlayKeys.externalizeResources := false
    )
    .settings(TestNGPlugin.testNGSettings: _*)
    .settings(
        aggregate in test := false,
        aggregate in dist := false,
        aggregate in jacoco.cover := false,
        TestNGPlugin.testNGSuites := Seq("test/resources/testngUnit.xml")
    )
    .settings(jacoco.settings: _*)
    .settings(
        parallelExecution in jacoco.Config := false,
        jacoco.reportFormats in jacoco.Config := Seq(de.johoop.jacoco4sbt.XMLReport())
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


lazy val jophielcommons = RootProject(file("../jophielcommons"))
