config { }

node(){
	git.checkout { }
	
	catchError {
		maven {	}
	}

        reportIssues()
	
        notify { slackChannel = "#somtoday-builds" }
}
