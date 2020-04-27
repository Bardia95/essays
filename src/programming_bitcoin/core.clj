(ns programming-bitcoin.core
  (:require [clojure.spec.alpha :as s]))

(defn modpow [n p m]
  (.modPow (biginteger n) (biginteger p) (biginteger m)))

(defn fermat-test [n]
  (let [a (inc (rand-int (dec n)))]
    (= (modpow a n n) a)))

(defn prime? [n]
  (every? true? (repeat 50 (fermat-test n))))

(defn make-finite-field [p]
  (if (s/valid? ::prime p)
    (into (sorted-set) (range 0 p))
    "Please supply a prime"))

(defprotocol FieldOperations
  (=f    [x y])
  (not=f [x y])
  (+f    [x y])
  (-f    [x y])
  (*f    [x y])
  (divf  [x y])
  (**f   [x k]))

(defn assert= [p p2]
  (assert (= p p2) "Fields need to be of the same prime order"))


(defrecord FieldElement [e p]
  FieldOperations
  (=f [x {e2 :e
          p2 :p}]
    (and (= e e2) (= p p2)))
  (not=f [x {e2 :e
             p2  :p}]
    (or (not= e e2) (not= p p2)))
  (+f [x {e2 :e
          p2 :p}]
    (FieldElement. (mod (+ e e2) p) p))
  (-f [x {e2 :e
          p2 :p}]
    (assert= p p2)
    (FieldElement. (mod (- e e2) p) p))
  (*f [x {e2 :e
          p2 :p}]
    (assert= p p2)
    (FieldElement. (mod (* e e2) p) p))
  (divf [x {e2 :eg
            p2 :p}]
    (assert= p p2)
    (FieldElement. (int (mod (* e (modpow e2 (- p 2) p)) p)) p))
  (**f [x k]
    (let [k (mod k (dec p))]
      (modpow e k p))))


(s/def ::zero-or-more #(>= % 0))
(s/def ::prime prime?)

(defn make-field-element [e p]
  (if (and (< e p) (s/valid? ::zero-or-more e) (s/valid? ::prime p))
    (FieldElement. e p)
    (println "Invalid Field Element")))
