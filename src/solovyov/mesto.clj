(ns solovyov.mesto
  (:refer-clojure :exclude [update-in assoc-in get-in])
  (:require [clojure.core :as cj]))

(def world (atom {}))

(defn cartesian-product
  "All the ways to take one item from each sequence"
  [seqs]
  (let [v-original-seqs (vec seqs)
        step
        (fn step [v-seqs]
          (let [increment
                (fn [v-seqs]
                  (loop [i (dec (count v-seqs)), v-seqs v-seqs]
                    (if (= i -1) nil
                        (if-let [rst (next (v-seqs i))]
                          (assoc v-seqs i rst)
                          (recur (dec i) (assoc v-seqs i (v-original-seqs i)))))))]
            (when v-seqs
              (cons (map first v-seqs)
                    (lazy-seq (step (increment v-seqs)))))))]
    (when (every? first seqs)
      (lazy-seq (step v-original-seqs)))))

;; utility

(defn- multi-by-condition
  [data condition]
  (filter #(= condition (select-keys % (keys condition))) data))

(defn- multi-get
  [data condition]
  (if (map? condition)
    (multi-by-condition data condition)
    (if (integer? condition)
      [(data condition)]
      [(condition data)])))

(defn- multi-path
  [data found condition]
  (if-not (map? condition)
    [condition]
    (map #(.indexOf data %) found)))

(defn gather-paths-bits
  [data [condition & path]]
  (let [multi-data (multi-get data condition)
        bit (multi-path data multi-data condition)]
    (if path
      (apply conj [bit] (mapcat #(gather-paths-bits % path) multi-data))
      [bit])))

(defn paths-to
  [data path]
  (cartesian-product (gather-paths-bits data path)))

;; events notification

(defn- notify
  ([data path]
     (notify data path (data :handlers)))
  ([data path handlers]
     (when-not (empty? handlers)

       (doseq [f (handlers :_handlers [])]
         (f data path))

       (if-not (empty? path)
         (let [condition (first path)
               rest-path (rest path)]
           (notify (data condition) rest-path (handlers condition)))))))

;; api

(defn assoc-in
  [path value]
  (doseq [real-path (paths-to @world path)]
    (swap! world cj/assoc-in real-path value)
    (notify @world real-path)))

(defn update-in
  [path f & args]
  (doseq [real-path (paths-to @world path)]
    (apply swap! world cj/update-in real-path f args)
    (notify @world real-path)))

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

(defn on
  [path handler]
  (let [full-path (cons :handlers path)
        handlers-path (cons :handlers (conj path :_handlers))]
    (if-not (get-in full-path)
      (swap! world cj/assoc-in handlers-path #{}))
    (swap! world cj/update-in handlers-path conj handler)))
