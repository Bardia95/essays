(ns programming-bitcoin.core
  (:require [clojure.spec.alpha :as s]))

(defn modpow [b e m]
  (.modPow (biginteger b) (biginteger e) (biginteger m)))

(defn fermat-test [n]
  (let [a (inc (rand-int (dec n)))]
    (= (modpow a n n) a)))

(defn prime? [n]
  (every? true? (take 50 (repeatedely #(fermat-test n)))))

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


(defrecord FieldElement [e p])

(extend-type FieldElement
  FieldOperations
  (=f [{e :e p :p} {e2 :e p2 :p}]
    (and (= e e2) (= p p2)))
  (not=f [{e :e p :p} {e2 :e p2 :p}]
    (or (not= e e2) (not= p p2)))
  (+f [{e :e p :p} {e2 :e p2 :p}]
    (assert= p p2)
    (FieldElement. (mod (+ e e2) p) p))
  (-f [{e :e p :p} {e2 :e p2 :p}]
    (assert= p p2)
    (FieldElement. (mod (- e e2) p) p))
  (*f [{e :e p :p} {e2 :e p2 :p}]
    (assert= p p2)
    (FieldElement. (mod (* e e2) p) p))
  (divf [{e :e p :p} {e2 :e p2 :p}]
    (assert= p p2)
    (FieldElement. (int (mod (* e (modpow e2 (- p 2) p)) p)) p))
  (**f [{e :e p :p} k]
    (let [k (mod k (dec p))]
      (FieldElement. (modpow e k p) p))))


(s/def ::zero-or-more #(>= % 0))
(s/def ::prime prime?)

(defn make-field-element [e p]
  (if (and (< e p) (s/valid? ::zero-or-more e) (s/valid? ::prime p))
    (FieldElement. e p)
    (println "Invalid Field Element")))
