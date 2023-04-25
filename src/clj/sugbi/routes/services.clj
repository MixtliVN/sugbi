(ns sugbi.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [sugbi.catalog.db :as catalog.db]
    [sugbi.catalog.core :as catalog.core]
    [sugbi.middleware.formats :as formats]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]))

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 coercion/coerce-exceptions-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
            {:url "/api/swagger.json"
             :config {:validator-url nil}})}]]

   ["/catalog" {:swagger {:tags ["Catalog"]}}
    ["/books"
     ["" {:get    {:summary    "search something in to the catalog"
                   :parameters {:query {:q string?}}
                   :responses  {200 {:body some?}}
                   :handler    (fn [{{{:keys [q]} :query} :parameters}]
                                 {:status 200
                                  :body   (catalog.core/enriched-search-books-by-title
                                           q
                                           #{:title               :full-title
                                             :subtitle            :publishers
                                             :publish-date        :weight
                                             :physical-dimensions :genre
                                             :subjects            :number-of-pages})})}
          :post   {:summary    "add a book title to the catalog"
                   :parameters {:body {:isbn  string?
                                       :title string?}}
                   :responses  {200 {:body some?}}
                   :handler    (fn [{{{:keys [isbn title]} :body} :parameters}]
                                 {:status 200
                                  :body   (catalog.db/insert-book! {:isbn  isbn
                                                                    :title title})})}
          :delete {:summary    "delete a book title of the catalog"
                   :parameters {:body {:isbn string?}}
                   :responses  {200 {:body some?}}
                   :handler    (fn [{{{:keys [isbn]} :body} :parameters}]
                                 {:status 200
                                  :body   {:deleted (catalog.db/delete-book! {:isbn isbn})}})}}]
     ["/:isbn" {:get {:summary    "get a book info by its isbn"
                      :parameters {:path {:isbn string?}}
                      :responses  {200 {:body some?}}
                      :handler    (fn [{{{:keys [isbn]} :path} :parameters}]
                                    {:status 200
                                     :body   (catalog.core/get-book
                                              isbn
                                              #{:title               :full-title
                                                :subtitle            :publishers
                                                :publish-date        :weight
                                                :physical-dimensions :genre
                                                :subjects            :number-of-pages})})}}]]]])
