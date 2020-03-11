name := "HousingAssociationPlayFramework"
 
version := "1.0" 
      
lazy val `housingassociationplayframework` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

libraryDependencies += "com.typesafe.play" %% "play" % "2.7.3"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.10"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "4.0.0"
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m"