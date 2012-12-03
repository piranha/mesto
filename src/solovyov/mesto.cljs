(ns solovyov.mesto
  (:refer-clojure :exclude [update-in assoc-in get-in])
  (:require [cljs.core :as cj]))

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

(defn- index-of ;; cljs PersistentVector has no .indexOf method :(
  [coll item]
  (loop [coll coll
         idx 0]
    (let [current (first coll)]
      (if-not current
        -1
        (if (= item current)
          idx
          (recur (rest coll) (inc idx)))))))

(defn- matches-map
  [item condition]
  (= condition (select-keys item (keys condition))))

(defn- multi-by-map
  [data condition]
  (filter #(matches-map % condition) data))

(defn- multi-by-fn
  [data condition]
  (let [found (filter condition data)]
    (if (map? data)
      (map second found)
      found)))

(defn- multi-get
  [data condition]
  (if data
    (cond
     (map? condition) (multi-by-map data condition)
     (fn? condition) (multi-by-fn data condition)
     :else (if-let [rv (data condition)] [rv] []))

    []))

(defn- multi-path
  [data found condition]
  (if (or (keyword? condition) (integer? condition))
    ;; simple conditions are going without changes
    [condition]
    ;; FIXME: indexOf limits us here only to arrays... should maps filtered by
    ;; some condition be supported? Probably so.
    (map #(index-of data %) found)))

(defn- gather-paths-bits
  [data [condition & path]]
  (let [multi-data (multi-get data condition)
        bit (multi-path data multi-data condition)]
    (if path
      (apply conj [bit] (mapcat #(gather-paths-bits % path)
                                (if-not (empty? multi-data) multi-data [nil])))
      [bit])))

(defn- paths-to
  [data path]
  (cartesian-product (gather-paths-bits data path)))

;; events notification

(defn- notify
  ([data path]
     (notify data path (data :handlers)))
  ([data path handlers]
     (when-not (empty? handlers)

       (doseq [f (handlers :-handlers [])]
         (f data path))

       (if-not (empty? path)
         (let [condition (first path)
               rest-path (rest path)
               changed-data (data condition)]

           (doseq [key (keys handlers)]
             (if (or (and (map? key) (matches-map changed-data key))
                     (and (fn? key) (true? (key changed-data)))
                     (= key condition))
               (notify changed-data rest-path (handlers key)))))))))

;; api

(defn assoc-in
  "Associates a value in a nested associative structure, where path is a
  sequences of keys and filters and value is a new value. Returns a new nested
  structure.

  If any levels do not exists, creates hash-maps for keys and fails for filters."
  [world path value]
  (doseq [real-path (paths-to @world path)]
    (swap! world cj/assoc-in real-path value)
    (notify @world real-path))
  @world)

(defn update-in
  "Updates a value in a nested associative structure, where path is a sequences
  of keys and filters, f is a function that will take the old value and any
  supplied args and return the new value. Returns a new nested structure.

  If any levels do not exists, creates hash-maps for keys and fails for filters."
  [world path f & args]
  (doseq [real-path (paths-to @world path)]
    (apply swap! world cj/update-in real-path f args)
    (notify @world real-path))
  @world)

(defn all-in
  "Returns all found values in a nested associative structure, where ks is a
  sequence of keys and filters. Returns nil if the key is not present, or the
  not-found value if supplied."
  [data [condition & path]]
  (if condition
    (let [multi-data (multi-get data condition)]
      (if (empty? path)
        multi-data
        (mapcat #(all-in % path) multi-data)))))

(defn get-in
  "Returns the value in a nested associative structure, where ks is a sequence
  of keys and filters. Returns nil if the key is not present, or the not-found
  value if supplied."
  ([data path]
     (first (all-in data path)))
  ([data path not-found]
     (let [found (all-in data path)]
       (if (= 0 (count found))
         not-found
         (first found)))))

(defn on
  "Calls provided handler when change occurs in path (i.e. if changes appear at
  or inside whatever happens to be matched by given path).

  Handler receives two arguments - first (data) holds data matched by
  subscription path and second (path) holds relative path in data argument to
  actual changed value."
  [world path handler]
  (let [full-path (cons :handlers path)
        handlers-path (cons :handlers (conj path :-handlers))]
    ;; regular core/get-in here, because we set literal items (filters as
    ;; keys in a map of handlers, not as filters)
    (if-not (cj/get-in @world full-path)
      (swap! world cj/assoc-in handlers-path #{}))
    (swap! world cj/update-in handlers-path conj handler)))
