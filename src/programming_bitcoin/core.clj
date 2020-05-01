(ns programming-bitcoin.core)

(defn modpow [b e m]
  (mod (reduce #(mod (* %1 %2) m) (repeat e b)) m))

(defn ** [b e]
  (reduce * (repeat e b)))

(defn fermat-test [n]
  (let [a (inc (rand-int (dec n)))]
    (= (modpow a n n) a)))

(defn prime? [n]
  (every? true? (take 50 (repeatedly #(fermat-test n)))))

(defn make-finite-field [p]
  (if (prime? p)
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


(defn make-field-element [e p]
  (if (and (<= 0 e) (< e p) (prime? p))
    (FieldElement. e p)
    (println "Invalid Field Element")))


(defrecord Point [x y a b])

(defprotocol PointOperations
  (=p    [x y])
  (not=p [x y])
  (+p    [x y])
  (-p    [x y])
  (*p    [x y])
  (divp  [x y])
  (**p   [x k]))


(defn make-point [x y a b]
  (if (and (= x nil) (= y nil))
    (Point. x y a b)
    (if (not= (** y 2) (+ (** x 3) (* a x) b))
      (println (str "(" x ", " y ") is not on the curve."))
      (Point. x y a b))))
