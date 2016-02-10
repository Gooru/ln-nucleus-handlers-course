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
* Transactional layer to govern the transaction
* Transformer and/or writer layer so that output from DB layer could be transformed and written back to message bus - Partially done, may need to revisit later
* DB layer to actually do the operations
* Decide on using plain JDBC or light weight ORM like ActiveJDBC - Done - decided to use ActiveJDBC
* Extract UUID generation into separate utility which will also retry till gets unique UUID - handled by adding changing id columns to uuid datatype
* For update functionality, define list of updatable columns and update only those
* Revisit the logic to throw back errors for missing data in input data
* Add Move APIs
* Refactor delete check to include in query instead explicit
* While creating lesson, verify the unit is associated with proper course
* While creating Unit and Lesson, fetch owner of course and set as owner of Unit/Lesson
* Update sequence_id in Move APIs
* Authorization for all APIs
* input validation for move APIs
* input validation for reorder APIs
* Remove unused classes - AJResponseJsoTransformer


TODO
----
* Generalize the sanity check, validate request code to be reused (this is not needed now) - Low Priority
* Validate taxonomy from request data

 

To understand build related stuff, take a look at **BUILD_README.md**.


