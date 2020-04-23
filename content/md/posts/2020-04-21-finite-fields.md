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

5. If $$a$$ is in the set then $$a\^1$$ is also in the set and is defined by the value that makes $$ a * a^{-1} = 1 $$

This is called the multiplicative inverse.

Finite fields always have a prime order $$p$$ and the set will include integers between $$0$$ and $$p - 1$$:

$$F_p = \{0, 1, 2, ... p-1\}$$

Finite field of prime order $$7$$:"

$$F_7 = \{0, 1, 2, 3, 4, 5, 6\}$$


## Constructing a Finite Field in Clojure

## Modulo Arithmetic

## Finite Field Addition and Subtraction

## Coding Addition and Subtraction in Clojure

## Finite Field Multiplication and Exponentiation

## Coding Multiplication and Exponentiation in Clojure

## Finite Field Division
