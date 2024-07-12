boolean isRelease = env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'main-jee8'
String suffix = env.BRANCH_NAME == 'main-jee8' ? '-jee8' : ''

config {
	cronTrigger = ""
}

def mvn = new nl.topicus.MavenCommands()

node() {
	git.checkout {}

	catchError {
		if (isRelease) {
			stage('Release') {
				git.branch("current-build")

				def release = mvn.createBuildTimestamp('yyyy.MM.dd.HHmmss') + suffix
				def nextRelease = mvn.createBuildTimestamp('yyyy.MM') + "-SNAPSHOT"

				def skipRelease = sh ( script: "git log -1 | grep '@release\\.skip'", returnStatus: true )

				if ( skipRelease != 0 ) {
					maven.release {
						releaseVersion = release
						tagNameFormat = "vakkentabel-maven-plugin-$release"
						options = "-DignoreSnapshots=true -DdevelopmentVersion=$nextRelease -Darguments=\"-Denforcer.skip -DskipTests\""
					}

					sh """git remote add ssh-origin git@github.com:topicusonderwijs/vakkentabel-maven-plugin.git || echo This is fine"""

					git.push("ssh-origin", "vakkentabel-maven-plugin-$release")
				} else {
					echo "Skipping release due to @release.skip in commit message"
					maven {
						goals = 'verify'
					}
				}
			}
		} else {
			maven {
				goals = 'verify'
			}
		}
	}

	reportIssues()

	notify {
		slackChannel = "#somtoday-dev"
		emailNotificationRecipients = 'jeroen.steenbeeke@topicus.nl'
	}
}
