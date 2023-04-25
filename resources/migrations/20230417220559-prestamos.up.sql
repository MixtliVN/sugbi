create schema prestamos;
--;;
create table prestamos.book_availability (
  availability int not null,
  book_id bigint not null unique references catalog.book
);
--;;
create table prestamos.users (
  user_id bigint generated always as identity primary key,
  full_name text not null unique
);
--;;
create table prestamos.book_users (
  user_id bigint not null references prestamos.users,
  book_id bigint not null references catalog.book,
  return_date date not null check (((return_date - current_date) <= 14) and ((return_date - current_date) > 0))
);
