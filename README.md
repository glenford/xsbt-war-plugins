
xsbt-war-plugins
================

This is a set of war plugins for xsbt (sbt 0.10+)

The idea originally was to port my jetty-embed plugin from sbt 0.7.x
to sbt 0.10.x however the changes between the two forced a reconsideration
as to structuring and implementation.

! WORK IN PROGRESS !
--------------------

There is a lot internally to be improved, in some areas a brute force approach
has been taken, simply to keep moving forward, they will be addressed later.

This documentation will improve in time.

To understand the options for running the embedded jetty wars please see:

  https://github.com/glenford/sbt-jetty-embed

Currently there is a bug that requires building the plugins twice.


How to use it
-------------

First clone the repository

  git clone git://github.com/glenford/xsbt-war-plugins.git


Build the plugins (see note above)

  cd xsbt-war-plugins
  xsbt package

Copy the plugin jars to your project/plugins/lib directory

  cp xsbt-war/target/scala-2.8.1.final/xsbt-war-plugin_2.8.1-0.1.jar ../myproject/project/plugins/lib
  cp xsbt-jetty-embed/target/scala-2.8.1.final/xsbt-jetty-embed_2.8.1-0.1.jar ../myproject/project/plugins/lib

Now you can create an embeded jetty version of a war by

  cd ../myproject
  xsbt embed-jetty

  java -jar target/scala-2.8.1.final/myproject_2.8.1-0.1-jettyEmbed.war

If you want to change the jetty version (default is 6.1.21) override it in your build.sbt file, note it only supports jetty 6 & 7 at this point in time.

  jettyVersion := "7.3.0.v20110203"




License
-------

This work is licensed under the Apache 2.0 License





