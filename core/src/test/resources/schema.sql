-- This schema is automatically run on DB init. To explicitly configure this you could use this property below:
--      ---> spring.datasource.url=[...];INIT=RUNSCRIPT FROM 'classpath:schema.sql'
-- However, probably because of a good default configuration from Spring, if a schema.sql is present on the classpath,
-- it will execute it on initialisation.

-- This should not be necessary, because as I understand I have configured the in memory database to be recreated each
-- time. This script should also be running before auto DDL is executed, so I am uncertain why there is already a
-- hibernate_sequence existing. Either way, this is the simple fix which I don't understand why I need.
drop sequence if exists hibernate_sequence;
-- This is necessary because there is an issue with the auto DDL Hibernate generates, and I cannot figure out
-- why this occurs. It produces "create sequence hibernate_sequence start 1 increment 1;" - which is invalid syntax for H2.
-- At least, I believe the syntax is invalid. It seems to create a database but when retrieving a value from the sequence
-- we get a problem with the result set. Most likely H2 is creating the sequence but the start and increment are not
-- what Hibernate expects.
create sequence hibernate_sequence start with 1 increment by 1;