--create dictionary table
create table dictionary
(
  id serial primary key,
  value varchar not null,
  unique (value)
);

--create roles table
create table roles
(
  role_id serial primary key ,
  role_name varchar not null,
  unique (role_id, role_name)
);

--create rooms table
create table rooms
(
  room_id serial primary key ,
  room_name varchar not null ,
  open boolean default true not null ,
  drawer varchar(255) default null,
  guess varchar(255) default '/start',
  unique (room_name)
);

--create users table
create table users
(
  user_id serial primary key ,
  username varchar not null ,
  password varchar not null ,
  confirm_password varchar ,
  enabled boolean default true not null ,
  score integer default 0,
  unique (username)
);

--create users_roles table
create table users_roles
(
  user_id integer not null ,
  role_id integer not null ,
  foreign key (user_id) references users(user_id),
  foreign key (role_id) references roles(role_id)
);

--create rooms_user table
create table rooms_users
(
  room_id integer not null ,
  user_id integer not null ,
  foreign key (room_id) references rooms(room_id),
  foreign key (user_id) references users(user_id)
);