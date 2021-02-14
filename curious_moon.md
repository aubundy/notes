# A Curious Moon
## Postgres Basics
Postgres.app

`which psql` - locate Postgres binaries

`psql` - startup Postgres client

`\h` - help menu

`createdb DATABASE` `dropdb DATABASE` - create or drop database

`\c DATABASE` - connect to database

```
drop table if exists master_plan;
CREATE TABLE master_plan(
  id serial primary key,
  the_date date,
  title varchar(100),
  description text
);
```

`DROP TABLE master_plan;`

`serial primary key` is Postgres specific and replaces the following SQL - 
```
create table master_plan(
  id integer not null
  --...
);

create sequence master_plan_id_seq;
alter table master_plan
alter column id set default nextval('master_plan_ id_seq');

alter table master_plan
add constraint master_plan_pk primary key (id);
```

## ETL Process
The process of gathering, massaging, and loading data into a database


### Extraction

Pull relevant data out of systems in appropriate file types (CSV, etc.)

### Transformation 

This stage makes sure the data has correct typing, completeness, and accuracy 

* **Correct typing** - dealing with null values (—, N/A, etc.)
* **Completeness** - making sure our rows are not missing data 
* **Accuracy** - are the data points possible for what they’re representing

### Loading

Push data into normalized tables so we can query it 

Depending on data size or project needs we’ll use: shell scripts and make files, then Pandas, then a system like Kafka

Import everything as **text**. Get data into database first, then add data types

## SQL Scripts

`psql enceladus < build.sql`
`psql enceladus -f build.sql` - create a SQL script and push to psql 

### build.sql

```
drop table if exists master_plan;
create table master_plan( 
  start_time_utc text,
  duration text,
  date text,
  team text,
  spass_type text,
  target text,
  request_name text,
  library_definition text,
  title text,
  description text
);
```

This is an idempotent script. It will produce the same results every time. 

```
COPY master_plan
FROM '[PATH TO DIRECTORY]/master_plan.csv'
WITH DELIMITER ',' HEADER CSV;
```

The last line says that “,” separates the data points, that there’s a header, and it’s a csv file

## Schemas
Postgres has a hierarchy: 

1. **Cluster** - set of servers that execute the instructions
2. **Database** - the database
3. **Schemas** - usually multiple and the default is public
4. **Tables, views, functions, etc** - attached to a schema 

Don’t dump raw csv data into the public schema. If no schema is specified when creating a relation, it’s assumed to be public. 

```
create schema if not exists import;
drop table if exists import.master_plan; create table import.master_plan(
  start_time_utc text,
  duration text,
  date text,
  team text,
  spass_type text,
  target text,
  request_name text,
  library_definition text,
  title text,
  description text
);

COPY import.master_plan
FROM '[PATH TO]/master_plan.csv'
WITH DELIMITER ',' HEADER CSV;
```

## Using Make
Make turns one thing into another. Like header and code files into C objects and binaries, or individual SQL files that need combining to be executed. 

### Basics

You create **targets**, which are the processes to be executed. These get built through **recipes** (basically just shell commands). You can specify **prerequisites** for build order. All of this combines to make a **rule**. 

```
DB=enceladus
BUILD=${CURDIR}/build.sql
SCRIPTS=${CURDIR}/scripts
CSV='${CURDIR}/data/master_plan.csv'
MASTER=$(SCRIPTS)/import.sql
NORMALIZE = $(SCRIPTS)/normalize.sql

all: normalize
  psql $(DB) -f $(BUILD)

master:
  @cat $(MASTER) >> $(BUILD)

import: master
  @echo "COPY import.master_plan FROM
$(CSV) WITH DELIMITER ',' HEADER CSV;" >> $(BUILD)

normalize: import
  @cat $(NORMALIZE) >> $(BUILD)

clean:
  @rm -rf $(BUILD)
```

The labels: are the targets. Their recipe must follow on a new line, indented with a tab. 

`all` is the default target, and usually the first target defined. This will be executed if you don’t specify a take when using the `make` command

`clean` tears the build down and cleans out the build directory. Here, it is used to delete build.sql

`.PHONY` this target signals to Make that the other targets do not have physical artifacts. Make will check if something has been made already, and if there has and the source code hasn’t changed, it skips over it. It’s not used here. 

`normalize: import` specifies that normalize can’t be built without import being built first. Import needs master built first. 

`${CURDIR}` this is the current directory Make is running from 

Each target appends a little SQL to build.sql, except clean deletes it. All invokes the build file. 

When make is run, all is executed, but normalize is first because it’s a dependency. Import is before that and master before that. 

To run:

`make clean && make`