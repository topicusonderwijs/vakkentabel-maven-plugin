config { }

node(){
	catchError {
		git.checkout { }
	
		maven {	}

	        publishTestReports { }
	}
	
        notify { slackChannel = "#somtoday-builds" }
}
