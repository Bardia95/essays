{:title "Finite Fields"
 :layout :post
 :tags ["clojure", "bitcoin", "math"]
 :klipse true
 :toc true}
 


 This essay is a summary and exploration of Jimmy Song's [Programming Bitcoin](https://www.oreilly.com/library/view/programming-bitcoin/9781492031482/) in Clojure. The book's coding exercises are in Python, and as I am learning both Python and Clojure currently, I thought it would be fun to translate the exercises from Python to Clojure.
 
We start with finite fields. But why are they important to learning how to program Bitcoin? Well, Jimmy wants to teach us all the fundamentals so we know how Bitcoin's underlying components work. And guess what's at the heart of it all? That's right: finite fields.

Finite fields, along with elliptic curves (the topic covered in the next chapter), form the basis of the signing and verification algorithms that make Bitcoin transactions work, which are the atomic unit of value transfer of the network. So it's important to understand finite fields first before we learn the more advanced stuff built on top.

## Finite Field Definition

A finite field is a set of finite numbers of length $$p$$ otherwise known as the order of the set, which satisfies the following properties:

**1. Closed property**
- If $$a$$ and $$b$$ are in the set then $$a * b$$ and $$a + b$$ are also in the set.

- This means that we have to define addition and multiplication in a way that ensures the results stay within the set.

- $$\{0, 1, 2\}$$ is not closed under normal addition because $$1 + 2 = 3$$ and $$3$$ is not in the set.
    
- $$\{-1, 0, 1\}$$ is closed under normal addition and multiplication because no matter how you add or multiply the numbers, they will always be in the set.
    
**2. Additive identity**
- $$0$$ exists and has the property that $$ a + 0 = a$$.

**3. Multiplicative identity**
- $$1$$ exists and has the property that $$a * 1 = a$$. This is called the multiplicative identity.

**4. Additive inverse**
- If $$a$$ is in the set then $$-a$$ is also in the set and is defined by the value that makes $$a + (-a) = 0$$.

**5. Multiplicative inverse**
- If $$a$$ is in the set then $$a\^{1}$$ is also in the set and is defined by the value that makes $$ a * a^{-1} = 1 $$.

## Defining Finite Sets

Finite fields always have a prime order $$p$$ and the set will include integers between $$0$$ and $$p - 1$$, so:
    $$F_p = \{0, 1, 2, ... p-1\}$$
    
And a finite field of prime order $$7$$ would look like this:
    $$F_7 = \{0, 1, 2, 3, 4, 5, 6\}$$
    
## Constructing a Finite Field in Clojure

We're interested in modelling finite field elements for later use with elliptic curves. So to do that in Clojure we will define a record called FieldElement. For those unfamiliar with Clojure records, they are like a map or Python dictionary or JavaScript object. While they look like a map, you define the fields upfront like so:

```klipse-cljs
(defrecord FieldElement [e p])
```

Here, `e` refers to the integer representing the field element and `p` is the prime order of the finite field for that element. But we need a way to construct a valid `FieldElement` such that the element is between `0` and `p - 1` and that the prime number supplied is, in fact, prime.

Checking if a number is within a certain interval is trivial, but checking if a number is prime is a bit more involved. While we could use trial and error to check if the prime shares any divisors other than 1, this method does not scale up as well as it could, since the size of the problem grows linearly as we keep testing the input iteratively.

Alternatively, we could use Fermat's Primality Test, which allows us to prove an integer as a composite with 100% accuracy or as a prime with a very high degree of accuracy. It is based on Fermat's Little Theorem, which will prove useful later as well. Fermat's Little Theorem gives us a known pattern that all primes and very few composites follow. It asserts that given some prime number $$p$$ and some other integer $$a$$:

$$a\^{p} \mod p = a$$

It can also be written as:

$$a\^{p-1} \mod p = 1$$

If you do $$a\^{p} \mod p$$ and it returns anything other than $$a$$, you have what is called a composite witness—proof that the $$p$$ we chose cannot be prime. If you are not familiar with the modulo operator, we will be covering it soon, so don't worry. Anyways, the test works like this:

We take an integer $$p$$ whose primality we want to check as the input, then we generate a random integer $$a$$ in the range $$0 \leq a \leq p - 1$$. And then we can ask if $$a^p \mod p$$ returns $$a$$, if the answer is not $$a$$, then we know for sure it is a composite number and we can return that. If it does return $$a$$, we still do not know for sure that $$p$$ is prime. This is because of the fact that some composite numbers return $$a$$ for Fermat's Primality Test, and these are called Carmichael numbers, otherwise known as pseudoprimes. And the values for $$a$$ that show Carmichael numbers are primes are called fools because they fool us into thinking $$p$$ is prime when it is not.

To get over this hurdle, we can choose many different values for $$a$$, and this way we find many composite witnesses instead of values of $$a$$. It has been proven that the number of fools must divide the total size of the group we select from. What this means is that, at most, 50% of the elements in a finite field can be fools. And since $$a$$ is chosen randomly, the chance of finding a composite witness is at least 50%, and so after $$t$$ iterations, the probability no composite will be found with a composite number is at most $$\leq 1/2^t$$. So after $$50$$ iterations, the probability of mistakenly outputting a prime is $$1$$ in $$1125899906842624$$.

Fermat's test is much more efficient than simply checking greatest common divisors as the number of steps does not scale with the input. It is a logarithmic algorithm with a time complexity if $$O(log_2n)$$, meaning that the problem size splits in half each time the input size increases.

Here's how you would implement it in Clojure:

```klipse-cljs
(defn modpow [b e m] 
  (mod (reduce #(mod (* %1 %2) m) (repeat e b)) m))

(defn fermat-test
  "Integer -> Boolean"
  [p]
  (let [a (inc (rand-int (dec p)))]
    (= (modpow a p p) a)))

(defn prime? 
  "Integer -> Boolean"
  [p]
  (every? true? (take 50 (repeatedly #(fermat-test p)))))
  
(println (prime? 6))
(println (prime? 5))
  ```
  
We first define a modulo exponential function `modpow` to do modular exponentiation (we'll discuss this later), then the actual primality test `fermat-test`, and finally the `prime?` function, which runs `fermat-test` 50 times to be sure that our input is truly prime.

Now let's make a constructor function that creates a valid field element:

```klipse-cljs
(defn make-field-element 
  "Integer Integer -> FieldElement"
  [e p]
  (if (and (<= 0 e) (< e p) (prime? p))
    (FieldElement. e p)
    (println "Invalid input")))

(println (make-field-element -1 19))
(println (make-field-element 3 18))
(println (make-field-element 12 19))
```

All we're doing here is making sure our element `e` is from `0` up to, but not including, `p` before creating the `FieldElement`, otherwise we notify our user that their input is invalid. Ideally, you'd want to raise errors with custom messages for each validation but we're going to keep it relatively simple in this demo.

## Modulo Arithmetic

Modulo arithmetic is one of the tools to make finite field __closed__ under addition, subtraction, multiplication, and division. If you aren't familiar with the modulo operator, it just gives the remainder of a division:

$$27 \mod 7 = 6$$.
    
It can be useful to think of modulo as wrap-around mathematics or clock math. No matter how many hours you add to a time, the result will always be between 0h and 12h (inclusive). Similarly, the result of the modulo operation will always be between 0 and the divisor. Here are a couple exercises for you to try:

**a)** What is 46 hours from 3 o clock?

$$(46 + 3) \mod 12$$
    
**b)** What time was it 18 hours ago?

$$(9 - 18) \mod 12$$
    
**c)** What time will it be 98 minutes from now?

$$(2 + 98) \mod 60$$

Modulo properties make it very useful for bringing very large numbers into a relatively small range. For example:

$$28634972342763428 \mod 18 = 8$$. 

Modulo is used to define field arithmetic as most finite field operations use the modulo operator in some capacity. Now let's see how we use them to define new versions of addition and subtraction under a prime order.

## Finite Field Addition and Subtraction

To define finite field addition, we need to make sure that the result is still in the set—that it is __closed__. So we can use what we just learned, modulo arithmetic, to make addition closed:

$$a+_fb=(a+b) \mod p$$ where $$a, b \in F_p$$

We denote finite field operations like $$+_f$$ with $$_f$$ to avoid confusion with normal integer addition: $$+$$. And the symbol $$\in$$ just means "is an element of", so in this case the statement above is only valid when $$a$$ and $$b$$ are both in the field $$F$$ with a prime order of $$p$$.

By using modulo arithmetic, we guarantee that the result of this operation will always be in the field. So for $$F_19$$, we can define its addition operation as:

$$a+_fb=(a+b) \mod 19$$

And so, you can try these two examples:

**d)** $$14+_f8=(14+8) \mod 19$$

**e)** $$16+_f3=(16+3) \mod 19$$

If you did these exercises correctly, you could see that the results are both in the set, even though under normal addition, the results would be out of the bounds of the set. Now, with finite field addition, we can take any two numbers in the set, add them, and wrap around the end of the set to get the sum. We are creating our own addition operator so the results are do not submit easily to our intuitive sense of addition.

And if you remember the additive inverse, which we defined as $$a + (-a) = 0$$, because $$a \in F_p$$ implies that $$-_fa \in F_p$$, we can also define it as so:

$$-_fa = (-a) \mod p$$

And for $$F_{19}$$:

$$-_f9 = (-9) \mod 19 = 10$$

This means that:

$$9+_f10 = 0$$

Which turns out to be true.

So, similarly, we can define field subtraction, as such:

$$a-_fb=(a-b) \mod p$$ where $$a, b \in F_p$$

And you can test it out with these two examples:

**f)** $$12-_f16=(12-16) \mod 19$$

**g)** $$2-_f8=(2-8) \mod 19$$

## Coding Addition and Subtraction in Clojure

Now we can add finite field addition and subtraction to our record we defined earlier. Because we defined a record, it becomes an extensible `Type` that we can define functions for, similar to how class methods work in Object-Oriented Programming languages. We will provide the definition for field addition, and we'll leave it to you to figure out how to define field subtraction. All the code in this article is interactive, so you can edit the code below to make the test underneath it pass.

```klipse-cljs
(defprotocol FieldOperations
  (+f    [x y])
  (-f    [x y])
  (*f    [x y])
  (divf  [x y])
  (**f   [x k]))

(defn assert= [p p2]
  (assert (= p p2) "Fields need to be of the same prime order"))

(extend-type FieldElement
  FieldOperations
  (+f [{e :e p :p} {e2 :e p2 :p}]
      (assert= p p2)
      (FieldElement. (mod (+ e e2) p) p))
  (-f [{e :e p :p} {e2 :e p2 :p}]
      (assert= p p2)
      ;; Your code here
      ))

(def a (make-field-element 2 19))
(def b (make-field-element 7 19))
(println (assert (= {:e 14, :p 19} (-f a b)) "Please implement subtraction"))
(str "a + b = " (+f a b))
```

First we define a protocol, which how we can extend our type to support the new arithmetic rules. The protocol defines the function signatures without defining their implementation, so they can be defined later for different types. Then we are defining an `assert=` function to assert equality for primes for each finite field arithmetic operation. We define it in the global scope so the function definitions are less verbose.

Then we extend the `FieldElement` record type with the `FieldOperations` protocol and define addition, which takes two arguments: `x`, the first field element, to which the second argument `y` will be added to. We are using Clojure's built-in deconstruction feature to expose the values stored in the two field elements. And we return a `FieldElement` record, which can also be created with the `->FieldElement` positional factory constructor as well as the `map->FieldElement` factory constructor. The only difference is that, with the `map->FieldElement` constructor, we have to provide the fields in the form of the map, while with the other two, we can just provide the arguments to correspond to the fields in the order we defined them.

## Finite Field Multiplication and Exponentiation

We can define closed finite field multiplication in the same way we defined addition and subtraction before it. As multiplication is just repeated addition, we already know how to do it:

$$a*_fb=(a*b) \mod p$$ where $$a, b \in F_p$$

And we also know that exponentiation is just multiplying the same number many times, so we can easily define exponentiation as well:

$$a^k=(a^k) \mod p$$ where $$a \in F_p$$

Test your knowledge now by solving these equations in $$F_{53}$$:

**h)** $$60 * 22* 14$$

**i)** $$9^{23}$$

**j)** $$60 * 22 * 14 * 9^{23}$$

## Coding Multiplication and Exponentiation in Clojure

Now that we know how multiplication and exponentiation work for finite fields, let's define them in Clojure. We will give you the structure and let you finish the functions, and like before, you can see if you've done it right based on the tests below:

```klipse-cljs
(extend-type FieldElement
  FieldOperations
  (*f [{e :e p :p} {e2 :e p2 :p}]
      (assert= p p2)
      ;;Your code here
      )
  (**f [{e :e p :p} k]
      (assert= p p2)
      ;; Your code here
      ))
(println (assert (= {:e 14, :p 19} (*f a b)) "Please implement multiplication"))
(println (assert (= {:e 15, :p 19} (**f a 6022140923)) "Please implement exponentiation"))
```

## Finite Field Division

Unfortunately, finite field division is not as intuitive as addition, subtraction, multiplication or exponentiation. Finite fields are also closed under division and we know that, in normal math, division is the inverse of multiplication so we can use this to imply finite field division:

$$a*_fb = c \rightarrow c/_fb = a$$

And in $$F_{19}$$:

$$9*_f5 = 45 \mod 19 = 7 \rightarrow 7/_f5 = 9$$

But how do we calculate $$7/_f5 = 9$$ without first knowing that $$9*_f 5 = 7$$? The answer comes from Fermat's Little Theorem that we discussed before. Specifically, this formulation of it helps us define finite field division:

$$a^{p-1} \mod p = 1$$

And since division is the inverse of multiplication, we know:

$$a /_fb = a*_f(1/b) = a*_fb^{-1}$$

So now all we need to do to turn this to a multiplication problem is to find out what $$b\^{-1}$$ is. 

This is where we can apply Fermat's Little Theorem since we already know that $$b\^{p-1} \mod p = 1$$. So we can multiply $$b\^{-1}$$ by $$b\^{p-1}$$ to get $$b\^{-1} = b\^{p-2}$$. 

In $$F_{19}$$ this means that $$b\^{18} = 1$$, which means that $$b\^{17} = b\^{-1}$$ for all $$b > 0$$.

We can thus convert the division problem into one of multiplication and exponentiation. For $$F_{19}$$:

$$2/_f 7$$ 

$$= 2 *_f 7^{-1}$$

$$ = 2 *_f 7^{19-2}$$

$$= 2 * 7{17} \mod 19$$

$$= 465261027974414 \mod 19$$

$$ = 3$$

And generally:

$$a /_f b = a * b^{p-2} \mod p$$

Now let's code up our new finite division operation as a function in Clojure

```klipse-cljs
(extend-type FieldElement
  FieldOperations
  (divf [{e :e p :p} {e2 :e p2 :p}]
    (assert (= p p2))
    (FieldElement. (int (mod (* e (modpow e2 (- p 2) p)) p)) p)))
(println (str "a / b = ") (divf a b))
```

Here, we are multiplying the first field element by the result of the `modpow` function defined earlier that is more efficient than typical exponentiation. It runs the modulo function after each round of multiplication in the exponentiation. This is because, as you saw above, the exponents for finite field division can get very large very quickly. We then wrap the element in the `int` function because the result of the `modpow` function is a big integer and we want our `FieldElement` to have a normal integer as the element's type. And we're done.

## Conclusion

There you have it! We have covered the basics of finite fields and we wrote some Clojure to implement the concepts we learned. If you have any comments, concerns, feedback, or corrections, please do not hesitate to hit me up on Twitter.

Stay tuned for the next chapter summary and exploration where we'll be learning about elliptic curves!

<details><summary><strong>Answer Key</strong></summary><br>

**a)** 1

**b)** 3

**c)** 40

**d)** 3

**e)** 0

**f)** 15

**g)** 13

**h)** 36

**i)** 4

**j)** 38

</details>
