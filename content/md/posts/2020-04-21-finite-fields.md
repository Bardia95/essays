{:title "Finite Fields"
 :layout :post
 :tags ["clojure", "bitcoin"]
 :klipse true}
 
 
This essay is a summary and exploration of Jimmy Song's [Programming Bitcoin](https://www.oreilly.com/library/view/programming-bitcoin/9781492031482/) in Clojure. The book's coding exercises are in Python. But as I am learning both Python and Clojure currently, I thought it would be fun to translate the exercises from Python to Clojure.

We start with finite fields. But why are they important to learning how to program Bitcoin? Well, Jimmy wants to teach us all the fundamentals so we know how the Bitcoin's underlying components work. And guess what's at the heart of it all? That's right: finite fields.

Finite fields along with elliptic curves (the topic covered in the next chapter) form the basis of the signing and verification algorithms that make Bitcoin transactions work, which are the atomic unit of value transfer of the network. So it's important to understand finite fields first before we learn the more advanced stuff built on top.

## Finite Field Definition

A finite field is a set of finite numbers of length $$p$$ otherwise known as the order of the set, which satisfies the following properties:

1. If $$a$$ and $$b$$ are in the set then $$a * b$$ and $$a + b$$ are also in the set. This is called the closed property. This means that we have to define addition and multiplication in a way that ensures the results stay within the set
        - $$\{0, 1, 2\}$$ is not closed under normal addition because $$1 + 2 = 3$$ and $$3$$ is not in the set
        - $$\{-1, 0, 1\}$$ is closed under normal addition and multiplication because no matter how you add or multiply the numbers, they will always be in the set
    
2. 0 exists and has the property that $$ a + 0 = a$$. This is called the additive identity.

3. 1 exists and has the property that $$a * 1 = a$$. This is called the multiplicative identity.

4. If $$a$$ is in the set then $$-a$$ is also in the set and is defined by the value that makes $$a + (-a) = 0$$. This is called the additive inverse.

5. If $$a$$ is in the set then $$a\^1$$ is also in the set and is defined by the value that makes $$ a * a^{-1} = 1 $$. This is called the multiplicative inverse.

## Defining Finite Sets

Finite fields always have a prime order $$p$$ and the set will include integers between $$0$$ and $$p - 1$$:

$$F_p = \{0, 1, 2, ... p-1\}$$

Finite field of prime order $$7$$:"

$$F_7 = \{0, 1, 2, 3, 4, 5, 6\}$$

## Constructing a Finite Field in Clojure

We're interested in modelling finite field elements specifically for later use with elliptic curves. So to do that in Clojure we will define a record called FieldElement. For those unfamiliar with Clojure or the concept of a record, it's basically like a Clojure map or Python dictionary or JavaScript Object. It's based on key-value pairs like maps are but what makes records more powerful is that they leverage features of Clojure's host platform—-the Java Virtual Machine (JVM)--to provide better performance. They also provide class-like features such as fields that are in effect class properties and constructor functions.

To define a record 

```klipse-cljs
(defrecord FieldElement [el prime])
```

## Modulo Arithmetic


Modulo arithmetic is one of the tools to make finite field closed under addition, subtraction, multiplication, and division. If you aren't familiar with the modulo operator, it just gives the remainder of an operation: $$27 \mod 7 = 6$$.

It can be useful to think of modulo as wrap-around mathematics or clock math. No matter how many hours you add to a time, the result will always be between 0h and 12h (inclusive). Similarly, the result of the modulo operation will always be between 0 and the divisor. Here are a couple exercises for you to try: 

1. What is 46 hours from 3 o clock?
- $$(46 + 3) \mod 12$$
2. What time was it 18 hours ago?
- $$(9 - 18) \mod 12$$
3. What time will it be 98 minutes from now?
- $$(2 + 98) \mod 60$$

Modulo properties make it very useful for bringing very large numbers into a relatively small range. For example, $$28634972342763428 \mod 18 = 8$$. 

Modulo is used to define field arithmetic as most finite field operations use the modulo operator in some capacity. Now let's see how we use them to define new versions of equality and inequality. addition and subtraction under a prime order.

## Finite Field Equality and Inequality

## Coding Equality and Inequality in Clojure

One of my greatest joys in learning about programming is that in some languages, like Clojure and Python, you can redefine the mathematical operators for different classes.

## Coding Addition and Subtraction in Clojure

## Finite Field Multiplication and Exponentiation

## Coding Multiplication and Exponentiation in Clojure

## Finite Field Division
