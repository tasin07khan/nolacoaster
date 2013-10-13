README

Additional JARs:
In order to build and run this project, you need some additional JARs that for the moment I have
not added to the repository. I _think_ I should be able to add them, but I need to figure out
the licensing situation first. Until that time, here are the JARs that you should need to add:
	<classpathentry exported="true" kind="lib" path=".../gdata/java/lib/gdata-core-1.0.jar"/>
	<classpathentry exported="true" kind="lib" path=".../gdata/java/deps/guava-11.0.2.jar"/>
	<classpathentry exported="true" kind="lib" path=".../gdata/java/lib/gdata-spreadsheet-3.0.jar"/>
	<classpathentry exported="true" kind="lib" path=".../gdata/java/lib/gdata-spreadsheet-meta-3.0.jar"/>
	<classpathentry exported="true" kind="lib" path=".../gdata/java/lib/gdata-client-1.0.jar"/>
	<classpathentry exported="true" kind="lib" path=".../gdata/java/deps/jsr305.jar"/>

These should all basically be Google-able. (In fact, some of them I found on Google.) Please
let me know if I am missing any.

October 13, 2013

The first version that actually could be called useful is done. Here's the SVN log:

This is the first version of Megabudget that does something useful. It:
- populates the categories & months
- populates the total of the latest month
- you can select a category
- you can add an expense to that category

It has several notable problems
- no idea how it works when disconnected from the internet
- it is very slow, esp. at the beginning where it does multiple pulls from the
spreadsheet
- you can't select a month

Additionally it would be really nice to
- organize the categories by freq of use
- allow for offline expenses



August 3, 2013

I use a Google Docs spreadsheet to keep track of my personal budget. I'd like to occasionally add 
expenses from my phone while I'm on the go, so this is my attempt to do that. The basic idea is to
have adapters for getting the dates and categories so that if someone else were to want to use the
same basic program for their budget, they could write their own adapters and everything else would
just work. Let's see how thing goes...