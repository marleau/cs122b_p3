Project 3
=========

For CS 122B, 2011 Fall  
By Steven Neisius and Arielle Paek, Group 10

Testing Employee
----------------

Name: Tester CS122B  
Email: test@inter.net  
Password: pass  

Setup database
--------------

    $ cd into/project/folder
    $ mysql -u root -p
    mysql> drop database moviedb;
    mysql> create database moviedb;
    mysql> source createtable.sql;
    mysql> source raw_data.sql;

To compile
----------

    $ cd into/project/folder
    $ javac -d bin/ -classpath "$CLASSPATH:./lib/mysql-connector-java-5.1.17-bin.jar:" src/*.java

Keep in Mind
------------

To remove procedure

    mysql> DROP PROCEDURE add_movie;

To run procedure

    mysql> CALL add_movie(parameters here);

When writing procedures, can use IN/OUT before specifying parameters. IN is for input. OUT is output.
