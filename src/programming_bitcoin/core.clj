(ns programming-bitcoin.core)

(defn square [x]
  (* x x))

(defn mod-pow [n p m]
  (.modPow (biginteger n) (biginteger p) (biginteger m)))

(defn fermat-test [n]
  (let [a (inc (rand-int (dec n)))]
    (= (mod-pow a n n) a)))

(defn prime?
  ([n] (prime? n 50))
  ([n n-tests]
   (every? true? (take n-tests (repeatedly #(fermat-test n))))))

(defn make-finite-field [p]
  (if (prime? p)
    (into (sorted-set) (range 0 p))
    "Please supply a prime"))

(defprotocol FieldOperations
  (+ [x y])
  (- [x y])
  (* [x y])
  (/ [x y]))


(defrecord FieldElement [num prime]
  FieldOperations
  (+ [x {num2 :num
         prime2 :prime}]
    (assert (= prime prime2) "Cannot add number from two different fields")
    (FieldElement. (mod (+' num num2) prime) prime))
  (- [x {num2 :num
         prime2 :prime}]
    (assert (= prime prime2) "Cannot subtract number from two different fields")
    (FieldElement. (mod (-' num num2) prime) prime))
  (* [x {num2 :num
         prime2 :prime}]
    (assert (= prime prime2) "Cannot multiply number from two different fields")
    (FieldElement. (mod (*' num num2) prime) prime))
  (/ [x {num2 :num
         prime2 :prime}]
    (assert (= prime prime2) "Cannot divide number from two different fields")
    (FieldElement. (int (mod (*' num (mod-pow num2 (-' prime 2) prime)) prime)) prime)
    ))

(defn make-field-element [n p]
  (if (or (< n 0) (>= n p) (not (prime? p)))
    (println "Invalid Field Element")
    (FieldElement. n p)))
