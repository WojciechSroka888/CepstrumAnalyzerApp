# CepstrumAnalyzerApp
This is repository of CepstrumAnalyzerApp an Android application.

Contact me by email: ws.sroka@gmail.com
Google play store: https://play.google.com/store/apps/details?id=com.prog.gentlemens.cepstrumanalyzer
Website of this application: https://cepstralanalysisapp.webnode.com

This is application that I wrote in the object of final exam on my studies.
I decided to make this repository and share my code because maybe there are people that found this
subject interesting.

Additionally I would like to say that this application has the basic functionality (calculates basics audio components)
and is still under development, so there are many places with code and comments about code improvement - in one
word it is testing code.
**********************************************************************************************************************

This application calculates the cepstrogram, after this calculates mean frequency to get the basic frequency. Next
it calculates jitter and shimmer parameters (it is still under development). More scientific explanations
you can find by visiting website: https://sound.eti.pg.gda.pl/student/akmuz/03-f0.pdf or in books (PL):	Andrzej Kaczmarek, Analiza Sygnału Mowy w Foniatrii, Politechnika Gdańska, 2006. 
However yo can find many documentations in the Google ;)

**************HOW TO USE THE APPLICATION******************
In the first step you need to record your voice in the ONE PARTICULAR WAY 
-> by saying vowel "a", "e", "i", "o" or "u" by maximum  steady, stable and one - volume voice, during time 5 [s]

The second step is to analyze, so

*********************STEPS*************************
1. Set your name and surname
2. Set the type of vowel
3. Set time of record - default 5000 [ms], maximum 30000 [ms] (not necessary in normal usage)
4. Click record and record your steady and stable voice
5. Click next
6. Click analyze

THE RESULT shows two parts:
1. The graph - where each dots represents one calculated frequency
2. The fmean value - is the mean calculated by each dot frequency
3. The jitter and shimmer values - still under development

There are two buttons A and B. You can use this buttons to set a part of the record tha you would like
to calculate. Just touch the place on the graph and click A and touch the place on the graph and click B,
where point A is the start and point B is the end.
-> basically this application is dedicated to record the human voice (the frequency limitations in the application
is beetwen 40 - 400 [Hz]) but you can calculate the every basic frequency that is in range 40 [Hz] to 400 [Hz].
For example you can check the piano frequency beetwen voices from E1 to G4. You can take a look and see the
result of this on website:  https://cepstralanalysisapp.webnode.com.
