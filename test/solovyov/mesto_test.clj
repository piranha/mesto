(ns solovyov.mesto-test
  (:use clojure.test)
  (:require [solovyov.mesto :as me]))

(deftest simple-assoc
  (testing "Simple assoc"
    (let [data 1]
      (me/assoc-in [:some :path] data)
      (is (= data (me/get-in [:some :path]))))))

(deftest find-item
  (let [data {:id 1 :name "foo"}]
    (me/assoc-in [:items] [data])
    (testing "Finding by filter"
      (is (= data (me/get-in [:items {:id 1}])))
      (is (= "foo" (me/get-in [:items {:id 1} :name]))))
    (testing "Finding by index"
      (is (= data (me/get-in [:items 0]))))))

(deftest advanced-assoc
  (me/assoc-in [:items] [{:id 1 :name "foo"}])
  (testing "Assoc by filter"
    (me/assoc-in [:items {:id 1} :name] "bar")
    (is (= "bar" (me/get-in [:items {:id 1} :name])))))

(deftest advanced-update
  (me/assoc-in [:items] [{:id :foo :votes 1}])
  (testing "Update by filter"
    (me/update-in [:items {:id :foo} :votes] inc)
    (is (= 2 (me/get-in [:items {:id :foo} :votes])))))

(deftest simple-event-handler
  (me/on [:test] (fn [data path] (throw (Exception.))))
  (testing "Event by path"
    (is (thrown? Exception (me/assoc-in [:test] "foo")))))
