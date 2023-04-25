(ns sugbi.catalog.open-library-books
 (:require
  [camel-snake-kebab.core :as csk]
  [clj-http.client :as client]
  [medley.core :as medley]))


(def open-library-url
  "https://openlibrary.org")


(defn book-by-isbn-url
  [isbn]
  (str open-library-url "/isbn/" isbn ".json"))


(defn raw-book-info
  [isbn]
  (let [book-url (book-by-isbn-url isbn)]
    (-> book-url
        (client/get {:as :json})
        :body
        (#(medley/map-keys csk/->kebab-case %)))))


(defn book-info
  [isbn requested-fields]
  (let [relevant-fields #{:title               :full-title
                          :subtitle            :publishers
                          :publish-date        :weight
                          :physical-dimensions :genre
                          :subjects            :number-of-pages}
        raw-info        (raw-book-info isbn)]
    (-> raw-info
        (select-keys relevant-fields)
        (select-keys requested-fields)
        (assoc :isbn isbn))))


(defn multiple-book-info
  [isbns requested-fields]
  (doall
   (map
    #(book-info % requested-fields)
    isbns)))
