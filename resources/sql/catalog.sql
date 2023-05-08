-- :name insert-book! :! :1
insert into catalog.book (title, isbn, cover_image_data, cover_image_type)
values (:title, :isbn, :cover.data, :cover.type)
returning *;

-- :name delete-book! :! :n
delete from catalog.book where isbn = :isbn;

-- :name search :? :*
select isbn, true as "available"
from catalog.book
where title like :title;

-- :name get-book :? :1
select isbn, true as "available"
from catalog.book
where isbn = :isbn

-- :name get-book-cover :? :1
select isbn, cover_image_data, cover_image_type
from catalog.book
where isbn = :isbn
  and cover_image_type = :type;

-- :name get-books :? :*
select isbn
from catalog.book;
