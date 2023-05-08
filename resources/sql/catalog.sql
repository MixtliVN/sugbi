-- :name insert-book! :! :1
insert into catalog.book (title, isbn, cover_image_data, cover_image_type)
values (:title, :isbn, :cover.data, :cover.type)
returning *;

-- :name delete-book! :! :n
delete from catalog.book where isbn = :isbn;

-- :name search :? :*
select isbn, availability
from (catalog.book natural join prestamos.book_availability)
where title like :title;

-- :name get-book :? :1
select isbn, availability
from (catalog.book natural join prestamos.book_availability)
where isbn = :isbn

-- :name get-book-cover :? :1
select isbn, cover_image_data, cover_image_type
from catalog.book
where isbn = :isbn
  and cover_image_type = :type;

-- :name get-books :? :*
select isbn, availability
from catalog.book natural join prestamos.book_availability;

-- :name checkout-book! :! :1
insert into prestamos.book_users (user_id, book_id)
values (:user_id, :book_id, (current_date + 14))
returning *;

-- :name return-book! :! :n
delete from prestamos.book_users where (user_id = :user_id and book_id = :book_id);

-- :name get-book-lendings :? :1
select user_id, book_id
from prestamos.book_users
where user_id = :user_id
