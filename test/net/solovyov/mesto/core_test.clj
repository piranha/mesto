(ns net.solovyov.mesto.core-test
  (:use clojure.test)
  (:require [net.solovyov.mesto.core :as me]))

(deftest simple-assoc
  (testing "Simple assoc"
    (let [data 1]
      (me/assoc-in [:some :path] data)
      (is (= data (me/get-in [:some :path]))))))

(deftest find-item
  (let [data {:id 1 :name "yo"}]
    (me/assoc-in [:items] [data])
    (testing "Finding by filter"
      (is (= data (me/get-in [:items {:id 1}]))))
    (testing "Finding by index"
      (is (= data (me/get-in [:items 0]))))))
