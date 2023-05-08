(ns sugbi.catalog.handlers
  (:require
   [byte-streams :as byte-streams]
   [ring.util.http-response :as response]
   [sugbi.catalog.db :as catalog.db]
   [sugbi.catalog.core :as catalog.core]))


(defn search-books
  [request]
  (if-let [criteria (get-in request [:parameters :query :q])]
    (response/ok
     (catalog.core/enriched-search-books-by-title
      criteria
      catalog.core/available-fields))
    (response/ok
     (catalog.core/get-books
      catalog.core/available-fields))))


(defn- image-type
  [temp-file]
  (let [image-type (->> temp-file
                        :filename
                        (re-find #".*\.(jpg|png)")
                        second)]
    image-type))

(defn insert-book!
  [request]
  (let [{:keys [isbn title file]
         :as _book-info} (get-in request [:parameters :multipart])
        is-librarian?    (get-in request [:session :is-librarian?])]
    (def the-file file)
    (cond
      (not is-librarian?)      (response/forbidden {:message "Operation restricted to librarians"})
      (nil? (image-type file)) (response/unsupported-media-type {:message "Server just admits JPG or PNG files"})
      :else                    (response/ok
                                (select-keys
                                 (catalog.db/insert-book! {:isbn  isbn
                                                           :title title
                                                           :cover {:data (catalog.db/format-file file)
                                                                   :type (image-type file)}})
                                 [:isbn :title])))))


(defn delete-book!
  [request]
  (let [isbn          (get-in request [:parameters :path :isbn])
        is-librarian? (get-in request [:session :is-librarian?])]
    (if is-librarian?
      (response/ok
       {:deleted (catalog.db/delete-book! {:isbn isbn})})
      (response/forbidden {:message "Operation restricted to librarians"}))))


(defn get-book
  [request]
  (let [isbn (get-in request [:parameters :path :isbn])]
    (if-let [book-info (catalog.core/get-book
                        isbn
                        catalog.core/available-fields)]
      (response/ok book-info)
      (response/not-found {:isbn isbn}))))


(defn- image-content-type
  [type]
  (case type
    "jpg" "image/jpeg"
    "png" "image/png"))

(defn get-book-cover
  [request]
  (let [isbn       (get-in request [:parameters :path :isbn])
        media-type (get-in request [:parameters :path :ext])]
    (if-let [{image-data :cover_image_data
              image-type :cover_image_type} (catalog.db/get-book-cover {:isbn isbn
                                                                        :type media-type})]
      (-> image-data
          byte-streams/to-input-stream
          response/ok
          (response/header "Content-Type" (image-content-type image-type))
          (response/header "Content-Length" (count image-data)))
      (response/not-found {:isbn isbn}))))
