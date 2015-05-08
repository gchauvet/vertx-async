## Release Checklist

Here are the steps to follow when making a release.

* Update `HISTORY.md`
* Remove the "-SNAPSHOT" from the version "*0.1.2*-SNAPSHOT" in `pom.xml` (where *0.1.2* is the actual version, of course)
  * `$ mvn versions:set -DnewVersion=0.1.2`
* Add new contributors to `pom.xml`, if any
* Commit those changes as "Release 0.1.2"
  * `$ git add pom.xml`
  * `$ git commit -m "Release 0.1.2"`
* Tag commit as "v0.1.2" with short description of main changes
  * `$ git tag -a v0.1.2 -m "Description of changes"`
* Push to main repo on GitHub
  * `$ git push origin master`
  * `$ git push origin v0.1.2`
* Wait for build to go green
* Publish to Maven Central
  * `$ mvn clean deploy -P release`
* Updated the version to "*0.1.3*-SNAPSHOT" in `pom.xml` (where *0.1.3* is the next release version, of course)
  * `$ mvn versions:set -DnewVersion=0.1.3-SNAPSHOT`
* Commit those changes as "Preparing for next release"
  * `$ git add pom.xml`
  * `$ git commit -m "Preparing for next release"`
* Push to main repo on GitHub
  * `$ git push origin master`
