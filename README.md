[<img src="https://sling.apache.org/res/logos/sling.png"/>](https://sling.apache.org)

 [![Build Status](https://builds.apache.org/buildStatus/icon?job=Sling/sling-org-apache-sling-commons-logservice/master)](https://builds.apache.org/job/Sling/job/sling-org-apache-sling-commons-logservice/job/master) [![Test Status](https://img.shields.io/jenkins/t/https/builds.apache.org/job/Sling/job/sling-org-apache-sling-commons-logservice/job/master.svg)](https://builds.apache.org/job/Sling/job/sling-org-apache-sling-commons-logservice/job/master/test_results_analyzer/) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.commons.logservice/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.commons.logservice%22) [![JavaDocs](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.commons.logservice.svg)](https://www.javadoc.io/doc/org.apache.sling/org.apache.sling.commons.logservice) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Apache Sling OSGi LogService Implementation

This module is part of the [Apache Sling](https://sling.apache.org) project.

The "logservice" project implements the OSGi LogService implementation on top
of the SLF4J logging API. This bundle should be installed as one of the first
modules in the OSGi framework along with the SLF4J API and implementation and 
provided the framework supports start levels - be set to start at start level 1. 
This ensures the Logging bundle is loaded as early as possible thus
providing services to the framework and preparing logging.
