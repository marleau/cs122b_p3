Project 3
=========

For CS 122B, 2011 Fall  
By Steven Neisius and Arielle Paek, Group 10

To compile
----------

    cd into/project/folder
    javac -d bin/ -classpath "$CLASSPATH:.:./lib/mysql-connector-java-5.1.17-bin.jar:" src/*.java

Keep in Mind
------------

To remove procedure

    DROP PROCEDURE add_movie;

To run procedure

    CALL add_movie(parameters here);

When writing procedures, can use IN/OUT before specifying parameters. IN is for input. OUT is output.
