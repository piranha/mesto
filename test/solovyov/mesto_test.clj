(ns solovyov.mesto-test
  (:use clojure.test)
  (:require [solovyov.mesto :as me]))

(deftest simple-assoc
  (let [world (atom {})]
    (testing "Simple assoc"
      (me/assoc-in world [:some :path :foo] true)
      (is (true? (me/get-in @world [:some :path :foo]))))))

(deftest find-item
  (let [world (atom {})
        data {:id 1 :name "foo"}]
    (me/assoc-in world [:items] [data])

    (testing "Finding by filter"
      (is (= data (me/get-in @world [:items {:id 1}])))
      (is (= "foo" (me/get-in @world [:items {:id 1} :name]))))

    (testing "Finding by index"
      (is (= data (me/get-in @world [:items 0]))))

    (testing "Finding by filtering function"
      (is (= "foo" (me/get-in @world [:items #(= 1 (% :id)) :name]))))))

(deftest advanced-assoc
  (let [world (atom {})]
    (me/assoc-in world [:items] [{:id 1 :name "foo"}])
    (testing "Assoc by filter"
      (me/assoc-in world [:items {:id 1} :name] "bar")
      (is (= "bar" (me/get-in @world [:items {:id 1} :name]))))))

(deftest advanced-update
  (let [world (atom {})]
    (me/assoc-in world [:items] [{:id :foo :votes 1}])
    (testing "Update by filter"
      (me/update-in world [:items {:id :foo} :votes] inc)
      (is (= 2 (me/get-in @world [:items {:id :foo} :votes]))))))

(deftest event-handlers
  (let [world (atom {})]
    (testing "Simple event by path"
      (me/on world [:test] (fn [data path]
                             (throw (Exception.))))
      (is (thrown? Exception (me/assoc-in [:test] "foo"))))

    (testing "Event by map filter"
      (me/assoc-in world [:map] [{:id 1 :name "bar"}])
      (me/on world [:map {:id 1} :name]
             (fn [data path] (throw (Exception. data))))
      (is (thrown-with-msg? Exception #"foo"
            (me/assoc-in world [:map {:id 1} :name] "foo"))))

    (testing "Event by function filter"
      (me/assoc-in world [:fn] [{:id 1 :name "bar"}])
      (me/on world [:fn #(= 1 (:id %)) :name]
             (fn [data path] (throw (Exception. data))))
      (is (thrown-with-msg? Exception #"foo"
            (me/assoc-in world [:fn {:id 1} :name] "foo"))))))
