# A Curious Moon
## Postgres Basics
Postgres.app

`which psql` - locate Postgres binaries

`psql` - startup Postgres client

`\h` - help menu

`create database DATABASE` `drop database DATABASE` - create or drop database

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

### Organization

Also with Make, we can organize the different SQL commands into different files. Let’s divide our commands into

`import.sql` - creates import schema and loads CSV file

`normalize.sql` - split import table to lookups, etc. (next section)

## Normalization

The goal of normalization is to reduce repetition and therefore, disk space. It also sometimes speeds things up (comparing integers vs strings)

Our CSV file has the following columns:

* start_time_utc
• duration
• date
• team
• spass_type
• target
• request_name
• library_definition
• title
• description

We could create the following lookup tables out of these to avoid string repetition. SPASS evidently means Science Planning Attitude Spread Sheet.

* teams
• spass_types
• targets
• requests
• library_definitions

To build a lookup table, we get the distinct values, create a new table from this data, and add a primary key for use with a foreign key constraint. 

These lookup tables then refer back to a source/fact table. This structure works well with historical data, and this will form a star schema. 

## Importing Events
We’ll build a fact table for events that’ll go in our public schema. 

```
create table events(
  id serial primary key,
  time_stamp timestamptz not
  null, title varchar(500),
  description text,
  event_type_id int,
  spass_type_id int,
  target_id int,
  team_id int,
  request_id int
);
```


Only the time_stamp field can’t be empty. The lookup ids will be empty for now. 

### Dates

Importing dates will always be difficult. Formatting always differs. A lot of bugs have occurred due to leap year calculation issues. Postgres can fix a lot of date formatting issues. 

**timestamptz** - time stamp with a time zone. Postgres will store dates as UTC, but when the data is retrieved, it will convert it to have a time zone based on the server’s config file. If there’s no time zone info, Postgres assumes a local time. 

We can cast any date and time with **at time zone**

```
insert into events(
  time_stamp,
  title,
  description
)
select
  import.master_plan.date::timestamptz at time zone ‘UTC’,
  import.master_plan.title,
  import.master_plan.description
from import.master_plan;
```

## Lookup Tables

**distinct** helps pull only distinct values from a query. **into** sends the query results to a new table. 

```
drop table if exists [LOOKUP TABLE];
select distinct(THING)
  as description
  into [LOOKUP TABLE]
from import.master_plan;

alter table [LOOKUP TABLE]
add id serial primary key;
```

Now we just need to relate the lookup table back to facts table.

Joining two tables on text fields is very slow, but sometimes it's unavoidable. In our case, we can insert events from the import table and set the ids, all in one shot. This reduces 6 operations into 1. And we can update our foreign keys in our create table statement.

```
drop table if exists events;
create table events(
  id serial primary key,
  time_stamp timestamptz not null,
  title varchar(500),
  description text,
  event_type_id int references event_types(id),
  target_id int references targets(id),
  team_id int references teams(id),
  request_id int references requests(id),
  spass_type_id int references spass_types(id)
);

insert into events(
  time_stamp,
  title,
  description,
  event_type_id,
  target_id,
  team_id,
  request_id,
  spass_type_id
)
select
  import.master_plan.start_time_utc::timestamp, import.master_plan.title, import.master_plan.description, event_types.id as event_type_id,
  targets.id as target_id, teams.id as team_id, requests.id as request_id, spass_types.id as spass_type_id
from import.master_plan 
left join event_types
  on event_types.description
  = import.master_plan.library_definition 
left join targets
  on targets.description
  = import.master_plan.target 
left join teams
  on teams.description
  = import.master_plan.team
left join requests
  on requests.description
  = import.master_plan.request_name
left join spass_types
  on spass_types.description
  = import.master_plan.spass_type;
```

This doesn't import all of our data, though. The inner joins limits our selection, we need to use left joins (already replaced).

* inner joins only return results where there are matching rows in the joined table
* left join returns all the records in the "from" table, and fills out missing rows in the join table with nulls
* right join returns all the records in the join table, and pads in missing data with nulls
* full outer join returns all records and fills in all missing data with null
* cross join returns every row in the from table crossed with every row in the joining table. This is rarely used.

Put the CSV file into the data directory and two SQL files into a scripts directory.

import.sql. This drops and creates the import schema and creates the master_plan table - 
```
drop table if exists events cascade;
drop table if exists teams cascade;
drop table if exists targets cascade;
drop table if exists spass_types cascade;
drop table if exists requests cascade;
drop table if exists event_types cascade;

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

normalize.sql. This creates the lookups, creates the events table, and inserts everything into the events table - 
```
-- TEAM
drop table if exists teams;
select distinct(team)
as description
into teams
from import.master_plan;

alter table teams
add id serial primary key;

-- SPASS TYPES
drop table if exists spass_types;
select distinct(spass_type)
as description
into spass_types
from import.master_plan;

alter table spass_types
add id serial primary key;

-- TARGET
drop table if exists targets;
select distinct(target)
as description
into targets
from import.master_plan;

alter table targets
add id serial primary key;

-- EVENT TYPES
drop table if exists event_types;
select distinct(library_definition)
as description
into event_types
from import.master_plan;

alter table event_types
add id serial primary key;

--REQUESTS
drop table if exists requests;
select distinct(request_name)
as description
into requests
from import.master_plan;

alter table requests
add id serial primary key;

create table events(
  id serial primary key,
  time_stamp timestamp not null,
  title varchar(500),
  description text,
  event_type_id int references event_types(id),
  target_id int references targets(id),
  team_id int references teams(id),
  request_id int references requests(id),
  spass_type_id int references spass_types(id)
);

insert into events(
  time_stamp, 
  title, 
  description, 
  event_type_id, 
  target_id, 
  team_id, 
  request_id,
	spass_type_id
)	
select 
  import.master_plan.start_time_utc::timestamp, 
  import.master_plan.title, 
  import.master_plan.description,
  event_types.id as event_type_id,
  targets.id as target_id,
  teams.id as team_id,
  requests.id as request_id,
  spass_types.id as spass_type_id
from import.master_plan
left join event_types 
  on event_types.description 
  = import.master_plan.library_definition
left join targets 
  on targets.description 
  = import.master_plan.target
left join teams 
  on teams.description 
  = import.master_plan.team
left join requests 
  on requests.description 
  = import.master_plan.request_name
left join spass_types 
  on spass_types.description 
  = import.master_plan.spass_type;
```

Postgres cannot drop a table if other tables depend on it. Adding **cascade** to a drop statement will drop all of the dependent objects as well.

Instead of having the events table creation after the lookup tables are created, we could change the Makefile to be more granular.

## Flyby Queries

Find the precise flyby times for Enceladus.

Look through returned titles to find info on enceladus -

```
select targets.description as target,
time_stamp,
title
from events
inner join targets on target_id=targets.id;
```

Try looking for the term "flyby" or "fly by" in events, with no case sensitivity (use \x for expanded view) -

```
select targets.description as target,
event_types.description as event,
time_stamp,
time_stamp::date as date,
title
from events
left join targets on target_id=targets.id
left join event_types on event_type_id=event_
types.id
where title ilike '%flyby%'
or title ilike '%fly by%';
```

Use regex to target Titan flybys -

```
select targets.description as target,
time_stamp,
title
from events
inner join targets on target_id=targets.id
where title ~* '^T\d.*? flyby';
```

The first Enceladus flyby was Feb 17, 2005, but it's not appearing in the data. 

Target a specific date -

```
select target, title, date
from import.master_plan
where start_time_utc::date = '2005-02-17'
order by start_time_utc::date;
```

The flyby is there, but labeled incorrectly. Read about sargeable vs non-sargeable queries, full-text indexing, and views (and materialized view).

### The first Encelauds flyby

```
select
targets.description as target,
events.time_stamp,
event_types.description as event
from events
inner join event_types on event_types.id = events.
event_type_id
inner join targets on targets.id = events.target_id
where events.time_stamp::date='2005-02-17'
and targets.id = 28
order by events.time_stamp;
```

Found event type: Enceladus closest approach observation

Postgres searching through numbers a lot faster than strings. Always try to leverage key values.

### Sargeable vs. non-sargeable queries

"Search ARGument ABLE"

Sometimes you can't help searching for a string, so just make sure to use some kind of optimization. Sargeable queries can be optimized.

```
select *
from events
where description like 'closest%';
```

There's no wildcard before the term closest, so the query planner would be able to optimize this search if there was an index on the `description` field. But since there isn't, this is a non-sargeable query, and Postgres had to search every row in the table through a string comparison (sequential scan).

### Using views for easier querying

```
drop view if exists enceladus_events;
create view enceladus_events as
select
events.id,
events.title,
events.description,
events.time_stamp,
events.time_stamp::date as date,
event_types.description as event
from events
inner join event_types
on event_types.id = events.event_type_id
where target_id = 28
order by time_stamp;
```

We removed the targets join and the resulting info because the view is already labeled with the data the query is returning.

```
select * from enceladus_events
where date = '2005-03-09';
```

Postgres can output data to html.

```
\H
\o feb_2005_flyby.html
SELECT QUERY...
```

### Full-text queries

Add full-text search to our view - 

```
drop view if exists enceladus_events;
create view enceladus_events as
select
events.id,
events.title,
events.description,
events.time_stamp,
events.time_stamp::date as date,
event_types.description as event,
to_tsvector(events.description) as search
from events
inner join event_types
on event_types.id = events.event_type_id
where target_id = 28
order by time_stamp;
```

[More on to_tsvector()](https://www.postgresql.org/docs/current/textsearch-controls.html)

Find thermal results - 

```
select id, date, title
from enceladus_events
where date
between '2005-02-01'::date
and '2005-02-28'::date
and search @@ to_tsquery('thermal');
```

[Additional operators](https://www.postgresql.org/docs/current/functions-textsearch.html)

Aggregate query to see which team was most active on second flyby - 

```
select count(1) as activity, teams.description
from events
inner join teams on teams.id=team_id
where time_stamp::date='2005-03-09'
and target_id = 28
group by teams.description
order by activity desc;
```