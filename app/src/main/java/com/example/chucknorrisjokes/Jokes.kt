package com.example.chucknorrisjokes

object Jokes {
    var jokes = listOf<String>(
        "Chuck Norris can make a movie with 75 cents and a dirty look.",
        "Big Foot claims he has a couple pictures of Chuck Norris... All his friends think he's full of crap.",
        "December 1 2013 will be the day Chuck Norris gets a playstation 5.",
        "When Chuck Norris was asked if he believed that the world was going to end in 2012 he resonded: \"Depends how I'm feeling that day.\"",
        "Chuck Norris is the answer to all your problems.",
        "Chuck Norris' jock strap has 2 bag restraint holders.",
        "Chuck Norris always puts his occupation as Chuck Norris",
        "one the boy said HELP HELP ITS CHUCK NORRIS",
        "Chuck Norris can slit your throat with his pinkie toenail.",
        "Chuck Norris' favorite flavor of gum is Tarantula."
    )
        set(value){
            field = value
            JokeAdapter(this)
        }
}