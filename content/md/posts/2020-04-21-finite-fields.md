{:title "Finite Fields"
 :layout :post
 :tags ["clojure", "bitcoin"]
 :klipse true}
 
 
This essay is a summary and exploration of Jimmy Song's [Programming Bitcoin](https://www.oreilly.com/library/view/programming-bitcoin/9781492031482/) in Clojure. The book's coding exercises are in Python. But as I am learning both Python and Clojure currently, I thought it would be fun to translate the exercises from Python to Clojure.

We start with finite fields. But why are they important to learning how to program Bitcoin? Well, Jimmy wants to teach us all the fundamentals so we know how the Bitcoin's underlying components work. And guess what's at the heart of it all? That's right: finite fields.

Finite fields along with elliptic curves (the topic covered in the next chapter) form the basis of the signing and verification algorithms that make Bitcoin transactions work, which are the atomic unit of value transfer of the network. So it's important to understand finite fields first before we learn the more advanced stuff built on top.

## Finite Field Definition

So what is a finite field? Without getting too mathematical, a finite field is a set of finite numbers which satisfies the following 5 properties:


```klipse-cljs

```

## Defining Finite Sets

## Constructing a Finite Field in Clojure

## Modulo Arithmetic

## Finite Field Addition and Subtraction

## Coding Addition and Subtraction in Clojure

## Finite Field Multiplication and Exponentiation

## Coding Multiplication and Exponentiation in Clojure

## Finite Field Division
