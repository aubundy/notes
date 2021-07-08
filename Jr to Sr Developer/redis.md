# [Redis](https://redis.io/documentation)

A NoSQL in-memory database, so it is very fast. It stores data in key-value pairs, but there's a chance you will lose some data.

Use it when you have small pieces of data you need to access a lot.

## Intro to databases

Different databases are built to best store and access different data types.

### Relational databases

Data is stored in tables. And the relationship between tables must be defined before you can begin inserting data.

SQL lets us communicate with the database.

### NoSQL databases

Many different types (MongoDB, Cassandra, Redis, Neo4J). You do not have to define the schema first, this offers the developer a lot more flexibility.

MongoDB stores data in documents (everything about a user would be in their document). It also has its own query language

## Setup

`brew install redis`

`brew services start redis`

### node-redis

```
const redis = require('redis');
const redisClient = redis.createClient({
  host: keys.redisHost,
  port: keys.redisPort,
  retry_strategy: () => 1000
});
const redisPublisher = redisClient.duplicate();
```

## [Redis Commands](https://redis.io/commands)

~~`src/redis-server` - starts redis~~

`src/redis-cli` - access redis through terminal

`SET name "Godzilla"`

`GET name` - "Godzilla"

`EXISTS name` - 1

`DEL name` - 1

`EXPIRE name 10`

`INCRBY counter 33`

`DECR counter 1`

## Data types

`MSET a 2 b 5`

`MGET a b` - "2" "5"

Redis uses 5 data types: strings, hashes, lists, sets, and sorted sets

### Hashes - objects

Hashes are maps between string keys and string values

`HMSET user id 15 name "Johnny"`

`HGET user id` - "15"

`HGET user name` - "Johnny

`HGETALL user` - "id" "15" "name" "Johnny"

### Lists - linked lists

Useful for quickly adding elements to a list, but slow in getting data from the list

`LPUSH ourlist 10` - 1

`RPUSH ourlist "hello"` - 2

`LRANGE ourlist 0 1` - "10" "hello"

`LTRIM ourlist` - trims the list

`RPOP ourlist` - "hello"

### Sets

Unordered collection of strings. They will not store duplicate values

`SADD ourset 1 2 3 4 5` - 5

`SMEMBERS ourset` - "1" "2" "3" "4" "5"

`SISMEMBER ourset 5` - 1

### Sorted sets

Ordered (least to greatest) collection of strings. They will not store duplicate values

`ZADD team 50 "Wizards"` - 1

`ZADD team 40 "Pelicans"` - 1

`ZRANGE team 0 1` - "Pelicans" "Wizards"

`ZRANK team "Wizards"` - 1