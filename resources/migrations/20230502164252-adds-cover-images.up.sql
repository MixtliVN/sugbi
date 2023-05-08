alter table catalog.book
  add column cover_image_data bytea default null,
  add column cover_image_type text default null;
