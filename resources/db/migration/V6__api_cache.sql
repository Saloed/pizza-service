create table if not exists apicache
(
    resource   varchar(255) primary key,
    updated_at timestamp,
    data       text
);
