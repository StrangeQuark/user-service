# User service

The User Service project is a Spring Boot application that handles user creation, login, deletion, and more for websites

# Tools

The User Service uses Spring Boot and PostgreSQL

It is highly recommended to use IntelliJ for your IDE, as IntelliJ is the IDE used in development

# Installation

Once the User Service repository cloned and PostgreSQL is installed, users will need to create an "userservice" table and connect to the database using the following commands:

**psql**

**CREATE DATABASE userservice;**

**GRANT ALL PRIVILEGES ON DATABASE "userservice" TO \<YourUsername\>;**

**\c userservice**

Now the server can be started in IntelliJ by running the UserServiceApplication configuration
