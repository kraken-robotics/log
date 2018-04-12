# Log

## Maven installation

If you want to use this library in one of your maven project, add this to your pom.xml :

    <repositories>
        <repository>
            <id>log-pfg-mvn-repo</id>
            <url>https://raw.githubusercontent.com/PFGimenez/log/mvn-repo/</url>
        </repository>
    </repositories>

and

    <dependency>
        <groupId>pfg.log</groupId>
        <artifactId>log</artifactId>
        <version>[1.0,)</version>
    </dependency>



## Manual compilation

[![Build Status](https://travis-ci.org/PFGimenez/log.svg?branch=master)](https://travis-ci.org/PFGimenez/log)

You can compile it yourself. You will need a JDK and maven.

    $ git clone https://github.com/PFGimenez/log.git --depth 1
    $ cd log
    $ mvn install
