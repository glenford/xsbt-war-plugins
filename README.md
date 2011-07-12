
xsbt-war-plugins
================

This is a set of war plugins for xsbt (sbt 0.10+)

The idea originally was to port my jetty-embed plugin from sbt 0.7.x
to sbt 0.10.x however the changes between the two forced a reconsideration
as to structuring and implementation.

Please see the Wiki for usage information:

[https://github.com/glenford/xsbt-war-plugins/wiki/xsbt-war-plugins]



Building
--------

First clone the repo

	git clone git://github.com/glenford/xsbt-war-plugins.git


Build the plugins

	cd xsbt-war-plugins
	xsbt package


Build the test applications

	$ cd xsbt-jetty-embed-test
	$ cp ../xsbt-war/target/scala-2.8.1.final/xsbt-war-plugin_2.8.1-0.1.jar project/plugins/lib
	$ cp ../xsbt-jetty-embed/target/scala-2.8.1.final/xsbt-jetty-embed_2.8.1-0.1.jar project/plugins/lib
	$ xsbt embed-jetty
	$ java -jar target/scala-2.8.1.final/xsbt-jetty-embed-test_2.8.1-0.1-jettyEmbed.war 
	2011-07-12 13:00:02.269::INFO:  Logging to STDERR via org.mortbay.log.StdErrLog
	2011-07-12 13:00:02.270::INFO:  jetty-6.1.x
	2011-07-12 13:00:02.378::INFO:  Extract file:/data/home/glefor/UserSource/xsbt-jetty-embed-test/target/scala-2.8.1.final/xsbt-jetty-embed-test_2.8.1-0.1-jettyEmbed.war to /tmp/Jetty_0_0_0_0_8080_xsbt.jetty.embed.test_2.8.1.0.1.jettyEmbed.war____7mm45i/webapp
	2011-07-12 13:00:02.712::INFO:  NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet
	12-Jul-2011 13:00:02 com.sun.jersey.api.core.PackagesResourceConfig init
	INFO: Scanning for root resource and provider classes in the packages:
	   net.usersource.jaxrs
	12-Jul-2011 13:00:02 com.sun.jersey.api.core.ScanningResourceConfig logClasses
	INFO: Root resource classes found:
	   class net.usersource.jaxrs.Simple
	12-Jul-2011 13:00:02 com.sun.jersey.api.core.ScanningResourceConfig init
	INFO: No provider classes found.
	12-Jul-2011 13:00:02 com.sun.jersey.server.impl.application.WebApplicationImpl _initiate
	INFO: Initiating Jersey application, version 'Jersey: 1.3 06/17/2010 04:53 PM'
	2011-07-12 13:00:03.560::INFO:  Started SelectChannelConnector@0.0.0.0:8080

The xsbt-jetty-embed-override-test provides an example of how to provide your own startup class.


License
-------

This work is licensed under the Apache 2.0 License
Please see the LICENSE and NOTICE files




