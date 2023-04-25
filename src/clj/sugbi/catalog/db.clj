(ns sugbi.catalog.db
 (:require
  [camel-snake-kebab.core :as csk]
  [conman.core :as conman]
  [sugbi.db.core :as db]
  [medley.core :as medley]))

(conman/bind-connection db/*db* "sql/catalog.sql")


(defn matching-books
  [title]
  (map
   #(medley/map-keys csk/->kebab-case %)
   (search {:title (str "%" title "%")})))
