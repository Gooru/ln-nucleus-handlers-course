Nucleus Course
================

This is the Course handler for Project Nucleus. 

This project contains just one main verticle which is responsible for listening for course address on message bus. 

DONE
----
* Configured listener
* Provided a initializer and finalizer mechanism for components to initialize and clean up themselves
* Created a data source registry and register it as component for initialization and finalization
* Provided Hikari connection pool from data source registry
* Processor layer is created which is going to take over the message processing from main verticle once message is read
* Logging and app configuration

TODO
----
* Transactional layer to govern the transaction
* DB layer to actually do the operations
* Transformer and/or writer layer so that output from DB layer could be transformed and written back to message bus
* Decide on using plain JDBC or light weight ORM like ActiveJDBC

To understand build related stuff, take a look at **BUILD_README.md**.


