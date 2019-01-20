## About

This project was created to automatically allow testing programming submissions against a set of tests.
It accepts new checks from anybody, allowing students to collaborate and crowd-source a solid test suit (that is the goal, anyways ;) to collectively ensure their programs are not missing some edge cases.

## Features

* Create and manage Users

* Two types of checks:
  1. Source code:

     You can submit classes implementing the `Check` interface, which can analyse the bytecode, source code and java class file of a submission.
     As these checks are run *outside the sandbox*, each check that is submitted this way needs to be manually approved.
  2. Input-Output:
     This is a convenience check type which supplies some input, runs the program and verifies the output.
     As these checks can (hopefully) not be malicious, checks do not need to be approved but can be directly submitted and run.

* Check uploading is a public action that anybody with an account can do

* Everybody with an account can submit their own code (as source code, files or a zip)

* Viewing and editing checks

* Additionally each check has a *Category* assigned (like the exercise it belongs to) and you can choose the category to run your code against.

## Technology stack

* Spring Boot REST backend
* H2 as a simple flat-file persistent data storage
* Vue.js frontend
* Java tools api to compile the submitted code in memory

# Short overview over the project structure

## Layout in the repository
The repository has three submodules, all managed in the same git repository:

* `SimpleCodeTester-Backend`: The backend spring server
* `simplecodetester-frontend`: The vue frontend
* `SimpleCodeTester-Lib`: The library that powers the code introspection features of the backend

## How a code check request is processed

1. The server verifies the JWT token of the client is valid
2. (if applicable) The server decodes the user submitted zip and extracts all java files
3. The server fetches all checks to run and passes them, together with the source code, over to the library
4. The library spins up a new Slave VM and tells the slave the port number it should connect to
5. The slave is up and has finished basic initialization, connects to the passed port and sends a ready message
6. The library detects that message and sends over the task. This includes all checks to run (either their source code or input/output) and the user submitted code
7. The slave compiles the checks and user code in-memory using the java tools API  
    a. If the compilation fails the client sends a message to the server and kills itself
8. If the compilation was successful the slave runs all checks and reports the results
9. The library passes the check result back to the server, which sends a response to the client

If the slave does not answer within the timeout (30s by default) it will be forcibly killed. This is possible because the slave sent its PID in the "ready" message.

# Todos
### Server
* [ ] Figure out a nicer way to handle IO/normal checks in the server API
* [ ] Manually create a nice schema and do not rely on JPA+Hibernate being nice
  * [ ] Add `flyway` or a similar technology to allow seamless database schema upgrades
### Frontend
* [ ] Refactor list components to a common CRUD component, if possible
* [ ] Add editing functionality for category names

### General
* [ ] A lot more unit tests and a few integration tests

# Screenshots
![Submit code](https://i.imgur.com/EYdEbBO.png)

![Check Result](https://i.imgur.com/TD0ZrRr.png)
