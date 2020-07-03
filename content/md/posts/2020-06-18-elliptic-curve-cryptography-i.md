{:title "Elliptic Curve Cryptography I"
 :layout :post
 :tags ["clojure", "bitcoin", "math"]
 :toc true
 :author "Bardia Pourvakil"}

Welcome to part one of exploring [Programming Bitcoin](https://www.oreilly.com/library/view/programming-bitcoin/9781492031482/)'s third chapter on elliptic curve cryptography in Clojure. In this section, we will be combining the subjects of the previous two chapters: [Finite Fields](https://btcclj.com/posts-output/2020-04-21-finite-fields/) and [Elliptic Curves](https://btcclj.com/posts-output/2020-05-23-elliptic-curves/). Together, they make up the necessary ingredients to create the cryptographic primitives we need to build our signing and verification algorithms, which we will be making in [part two](https://btcclj.com/posts-output/2020-07-03-elliptic-cryptography-ii/)!

All the code can be found [here on Github](https://github.com/Bardia95/programming-bitcoin). I have made some major changes to the code from the first two chapters to get elliptic curve cryptography to work, so if you are following on with the code from the first two sections, just keep in mind that some of the functions have changed and I will not be going over all changes in this piece for brevity's sake—it's going to be a long one already.

## Elliptic Curves over Finite Fields

Previously, we saw how $$y^2 = x^3, when plotted on a graph, like the one below, creates an elliptic curve.

![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2F-k-NB8Yzzg.png?alt=media&token=f403822f-3a2c-430b-8f83-5885f518f332)

It turns out that the same math holds for elliptic curves over finite fields as for real numbers as shown above. But because finite fields are, well, finite, we do not get a nice continuous curve if we try and plot points from the elliptic curve equation over them. We end up getting a scatter plot that looks like this:

![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2FFQMbsHgG-S.png?alt=media&token=f61725ad-ccbc-4f93-be42-17ee42e29bd5)

By using finite field addition, subtraction, multiplication, division, and exponentiation, we can actually do point addition over this "curve". Although it may seem surprising that we can do this, there are certain common patterns found among mathematical abstractions such as the notion of fields.

We can verify that a point is on an elliptic "curve" over a finite field by calculating both sides of the elliptic curve equation. For instance, if we wanted to prove that the point $$(17, 64)$$ is on Bitcoin's curve $$y^2 = x^3 + 7$$, we just plug in the numbers and use the modulo arithmetic we learned before and get our answer:

$$y^2 = 64^2 \mod 103 = 79$$
$$x^3 + 7 = (17^3 + 7) \mod 103 = 79$$

## Coding Elliptic Curves over Finite Fields

I changed the code from the earlier chapters to support overloaded operators  for both finite fields and points so we can initialize points on elliptic curves over reals and finite fields using the same exact code.

In the refactor, it became necessary to start operating with big integers, and as such, we aren't going to use integer addition operations, and we will, instead, extend the `Number` type to use the `bigint` quoted form operators:

```
(ns programming-bitcoin.ecc
  (:refer-clojure :exclude [+ - * /])
  ...)

(defprotocol FieldOps
  (+   [x y])
  (-   [x y])
  (*   [x y])
  (/   [x y])
  ;; Renamed ** to pwr
  (pwr [x y])
  (zero [x]))

(extend-type Number
  FieldOps
  (+   [x y] (+' x y))
  (-   [x y] (-' x y))
  (*   [x y] (*' x y))
  ;; Since we are excluding the `/` operator, 
  ;; and there is no biginteger version,
  ;; we use the namespaced version of `/`
  (/   [x y] (clojure.core// x y)) 
  (pwr [x k] (math/expt x k))
  (zero [x] 0))
```

And because the `FieldElement` type also uses overloaded operators with their own definitions, point addition will also work out of the box. We can test this like so:

```
(ns programming-bitcoin.ecc-test
  (:refer-clojure :exclude [+ - * /])
  (:require
   [clojure.test :refer :all]
   [programming-bitcoin.ecc :refer :all]))

(deftest point-addition
  (let [prime 223
        a  (->FieldElement   0 prime) b  (->FieldElement   7 prime)
        x1 (->FieldElement 192 prime) y1 (->FieldElement 105 prime)
        x2 (->FieldElement  17 prime) y2 (->FieldElement  56 prime)
        x3 (->FieldElement 170 prime) y3 (->FieldElement 142 prime)
        x4 (->FieldElement  47 prime) y4 (->FieldElement  71 prime)
        x5 (->FieldElement  36 prime) y5 (->FieldElement 111 prime)
        p1  (->Point x1 y1 a b)
        p2  (->Point x2 y2 a b)
        p3  (->Point x3 y3 a b)
        p4  (->Point x4 y4 a b)
        p5  (->Point x5 y5 a b)]
    (testing "point addition over finite fields"
      (testing "different points"
        (is (= (+ p1 p2) p3)))
      (testing "same points"
        (is (= (+ p4 p4) p5))))))
```

## Scalar Multiplication

Point addition remains associative over finite fields, and thus we can add points to themselves, and doing it over and over again results in the operation we know as multiplication. It is called scalar multiplication in this case because the coefficient in front is a scalar number but the variable at hand is a vector of $$x$$ and $$y$$ coordinates. Now we can add some new notation, so instead of writing $$(47, 71) + (47, 71)$$ we can just write $$2(47, 71)$$.

One of the important properties of scalar multiplication is that it is near impossible to predict without calculating. In the diagram below from Programming Bitcoin, each point is labeled by the number of times the point was added to itself to get to that point. In other words, its scalar coefficient.

![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2FA_1v1DVox-.png?alt=media&token=bb6ec1c1-8877-46ff-8861-73d632357217)

If you were given a random point on the curve, $$xG$$ and you were asked how many multiples of $$G$$ it is, you wouldn't be able to. And while we can perform scalar multiplication easily, performing the reverse function, scalar division, becomes intractable. This is called the discrete logarithm problem and forms the basis of elliptic curve cryptography. Since scalar multiplication is just point addition with the same point, scalar exponentiation is just scalar multiplication applied over and over again. And in the context of elliptic curves, the discrete logarithm problem is the inability to reverse scalar exponentiation.

Let's consider the equation: $$Q = P^x \mod p$$, where $$p$$ is a large prime. Trying to solve for $$Q$$ in this equation is trivial when you know the point $$P$$ and the exponent $$x$$. But trying to solve for $$x$$ when you have just $$P$$ and $$Q$$ is infeasible when you have a large enough $$p$$. And it is called the __discrete logarithm problem__ because the inverse function that cannot be solved easily is $$x = log_pQ$$.

The other important thing about scalar multiplication is that, after a certain multiple, you reach the additive identity, what is commonly known as 0 in normal mathematics, but in the case of point addition is referred to as the point at infinity. And the critical feature about this property is that if you choose any generator point $$G$$ on the curve and scalar multiply until you reach the identity point, it will create a set, $${G, 2G, 3G, ...nG}$$, where $$nG = 0$$.

This set is actually called a group, and because $$n$$ is finite, it is more specifically, a finite cyclic group. The cyclic part comes from the fact that we cycle back after we get to the point at infinity since everything is being done$$\mod p$$. Groups only have a single operation, in this case, its point addition, which has the same properties of closure, invertibility, commutativity, identity, and associativity we expect from our intuitions about math.

Since the generator point is public, when the group generated by scalar multiplication of a point is small enough, we can just find the answer with a brute force search. But for Bitcoin's curve and others used in elliptic curve cryptography, the numbers are much bigger and thus the discrete logarithm problem becomes incomputable because the amount of time needed to solve it would be insurmountable.

Because of these mathematical properties of groups combined with the fact that scalar multiplication is easy to do but its inverse very difficult, we have the necessary ingredients for elliptic curve cryptography. Let's code scalar multiplication first and then we can go into defining Bitcoin's curve before  implementing our signing and verification algorithms.

## Coding Scalar Multiplication

With a naive implementation we could define `scalar-multiply` simply so:

```
(defn scalar-multiply
  [coeff point]
  (if (zero? coeff)
    (zero point)
    (reduce + (repeat coeff point))))

(extend-type Number
  FieldOps
  ...
  (* [x y] (if (number? y)
             (*' x y)
             (scalar-multiply x y))))

(defrecord Point [x y a b]
  FieldOps
  ...
  (zero [p] (Point. nil nil a b)))
```

But this version of the function has a linear time complexity, $$O(n)$$, and thus highly inefficient and unsuitable for the large exponents we deal with on Bitcoin's curve. So we need to define `scalar-multiply` in a way that allows us to cut down on the time as the problem scales. We can do this by implementing a technique called binary expansion that is significantly faster on larger inputs. 

If we wanted to compute the value of, say, $$10P$$, instead of doing nine repeated point additions with $$P$$, we can use binary expansion to reduce the problem size by half at each step and solve it in just four steps. Since point addition is associative we can calculate $$10P$$ like this:

$$P+P = 2P$$
$$2P+2P = 4P$$
$$4P+4P = 8P$$
$$2P+8P=10P$$

This is how you would implement this technique in Clojure:


```
(defn scalar-multiply [s p]
  (if (zero? s) (zero p)
      (loop [s s
             p1 p
             p2 (zero p1)]
        (let [e (even? s)
              s (quot s 2)]
          (if (zero? s)
            (+ p1 p2)
            (if e
              (recur s (+ p1 p1) p2)
              (recur s (+ p1 p1) (+ p1 p2))))))))
```

## Defining the Curve for Bitcoin

As mentioned before, cryptography requires massive primes that allow for similarly huge finite cyclic groups such that multiplication becomes impossible to reverse with even the world's most powerful computers. Elliptic curve cryptography security is fundamentally based on the assumption that computers cannot go through a meaningful portion of a curve's finite cyclic group.

To define an elliptic curve for public key cryptography, you need to specify five public parameters: the constants, $$a$$ and $$b$$, in the elliptic curve equation, the prime, $$p$$, of the finite field, the generator point, $$G$$, and the order of the group generated by $$G$$, $$n$$.

Bitcoin uses the __secp256k1__ curve, which we've already discussed as having the constants $$a = 0$$ and $$b = 7$$. This is a simple elliptic curve equation and there are many others with much larger $$a$$ and $$b$$ values. The fact that these numbers are so low has created a cause for concern amongst cryptographers. No one is sure where the parameters came from, as in, how they were chosen. More troubling is the fact that the curve was constructed by the NIST, a US government agency that was recently found to have made a backdoor in an elliptic curve random number generator. Despite these suspicions, many blockchain protocols and a significant number of corporations all rely on this curve.

The prime, $$p$$, used by the __secp256k1__ curve is equal to $$2\^{256} - 2\^{32} = 977$$. This $$p$$ was specifically chosen because of how close it is to $$2\^{256}$$ so all the coordinates that make up the points of the curve can be expressed in 256 bits or less, hence the "256" part in the curve's name. This fact is what gives elliptic curves a leg up on other public key cryptography systems. The private keys generated by Bitcoin can be represented in 256 bits but provides the same security as a 3000 bit Diffie-Hellman key. These savings are crucial for systems dealing with a significant amount of cryptographic operations.

While we can express $$2\^{256}$$ concisely with exponential notation, this number is incredibly large—on the scale of the order of the number of atoms in the observable universe. Brute forcing your way into a Bitcoin private key is thus computationally infeasible, as a trillion computers doing a trillion computations every trillionth of a second for a trillion years is still less than $$2\^{256}$$.

The generator, $$G$$ has an $$x$$-coordinate of `0x79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798$$` and a $$y$$-coordinate of `0x483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8$$`. And the order generated by the generator point, `n`, is `0xfffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141`. The order of the finite cyclic group is also close to $$2\^{256}$$ so that any scalar multiple can also be represented with 256 bits or less.

**__Coding secp256k1__**

Now we define our constants for the curve in the code. 

```
(def N 0xfffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141)


(def A (->FieldElement 0 P))


(def B (->FieldElement 7 P))


(def G (->Point 0x79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798
                0x483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8
                a
                b))
```

We can also create a constructor function `->S256Point` so we can initialize points on Bitcoin's curve without having to redefine the constants each time we create a `Point` record. In case we initialize the point at infinity, we also need to check if the `x` and `y` coordinates are not numbers so we do not initialize them as field elements.

```
(defn ->S256Point [x y]
  (let [x (if (number? x) (->FieldElement x P) x)
        y (if (number? y) (->FieldElement y P) y)]
    (->Point x y A B)))
```

Now we can define `G` with this new constructor function and leave out `a` and `b` from the function call:

```
(def G (->S256Point 0x79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798
                    0x483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8))
```

We can also adjust our `*` operator to be more efficient since we know the order of the finite cyclic group, `N`. Let's define a `S256Point?` function first to help us determine whether the point we are dealing with is on the __secp256k1__. curve Then we can `mod` by `N` because whatever the scalar is, it has to be under that prime order.

```
(defn S256Point? [{:keys [a b]}]
  (and (= a A) (= b B)))


(extend-type Number
  FieldOps
  ...
  (*   [x y] (cond
               (number? y) (*' x y)
               (S256Point? y) (scalar-multiply (mod x N) y)
               :else (scalar-multiply x y))))
```

## Conclusion

We now have what we need to start doing public key cryptography. Just to recap, we covered elliptic curves over finite fields, scalar multiplication, and defining Bitcoin's curve. In part two, we will learn the basics of public key cryptography, how signing and verification work, and sign and verify a message with elliptic curve cryptography in Clojure with what we've learned.
