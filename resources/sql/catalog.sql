-- :name insert-book! :! :1
insert into catalog.book (title, isbn) values (:title, :isbn)
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

-- :name get-books :? :*
select isbn
from catalog.book;
