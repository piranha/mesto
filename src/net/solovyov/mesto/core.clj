(ns net.solovyov.mesto.core
  (:refer-clojure :exclude [update-in assoc-in get-in assoc])
  (:require [clojure.core :as cj]))

(def world (atom {}))

;; utility

(defn- multi-get
  [data condition]
  (if (map? condition)
    (filter #(= condition (select-keys % (keys condition))) data)
    (if (integer? condition)
      [(data condition)]
      [(condition data)])))

;; api

(defn assoc-in
  [path value]
  (swap! world cj/assoc-in path value))

(defn all-in
  ([path] (all-in @world path))
  ([data [condition & path]]
     (if condition
       (let [multi-data (multi-get data condition)]
         (if (empty? path)
           multi-data
           (mapcat #(all-in % path) multi-data))))))

(defn get-in
  [path]
  (first (all-in path)))
