{:title "Elliptic Curves"
 :layout :post
 :tags ["clojure", "bitcoin", "math"]
 :toc true}

Welcome to the second part of our Clojure-based exploration of Programming Bitcoin by Jimmy Song. In the first part, we tackled finite fields, which, when combined with this essay's topic, elliptic curves produce elliptic curve cryptography—the basis of Bitcoin's signing and verification algorithms. The essential math we will learn in this section may seem difficult at first but it is very similar to what we learned before.
## **Definition**
I am sure all of you are familiar with the equation of a linear function:
$$y = mx + b$$
For those who need a refresher, it's the equation of a straight line, where $$m$$ is the __slope__ and $$b$$ is the __y-intercept__. And similarly you might also be familiar with the equation for a quadratic function: 
$$y = ax^2 + bx + c$$
And a cubic function: 
$$y = ax^3 + bx^2 + cx + d$$
The equation for an elliptic curve is very similar to these functions, especially the cubic function:
$$y^2 = x^3 + ax + b$$
And the graph for the elliptic curve equation $$y^2 = -x^3  - 5x + 10$$, defined by the constants $$a = -5, b = 10$$, looks like this:
![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2F-k-NB8Yzzg.png?alt=media&token=f403822f-3a2c-430b-8f83-5885f518f332)
The elliptic curve used in Bitcoin has the name __secp256k1__ and has the equation:
$$y^2 = x^3 + 7$$
And its graph looks like this:
![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2FZBrR1ZUnQQ.png?alt=media&token=79b2213b-48b1-48d8-b817-f0dea605aee3)
## **Coding Elliptic Curves in Clojure**
For reasons that will become clear in the next essay, we are only interested in points on the elliptic curve, and not the curve itself. So instead of modelling the curve itself in Clojure, we will be modelling points on a specific curve as a Clojure record, which we'll name `Point`. Since we know that curves are defined by their $$a$$ and $$b$$ constants, we can construct a `Point` with the `x` and `y` coordinates, and `a` and `b` constants:
```(defrecord Point [x y a b])```
We covered how records work in the last section, but for the uninitiated, you can think of them as an extensible key-value store with pre-defined fields
To ensure the point is actually on the curve when we initialize a new `Point`, we will define a constructor function, which will do just that:
```(defn **
  "Big integer exponentiation"
  [b e]
  (reduce * (repeat (bigint e) (bigint b))))

(defn make-pt [x y a b]
  (assert (= (** y 2) (+ (** x 3)) (* a x) b))
  (Point. x y a b))```
In a production app, we'd be doing more validation, using something like `clojure.core.spec` but that is outside the scope of this exploration. Now we can move on to the most important aspect of elliptic curves.
## **Point Addition**
Elliptic curves are useful to cryptography because of a special property of adding points on a curve. Point addition involves performing an operation on two points on the curve to get a third point, which is also on the same curve. While it is not the same as normal addition, it is called so, because point addition shares some of the same intuitions we associate with addition.
As it turns out, every elliptic curve has the characteristic that a line will intersect it at one point or three points, other than in the case of a few exceptions.
Here's what it looks like when the line intersects at three points:
 ![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2Fnqa4CjGYVM.png?alt=media&token=6ae72116-d452-428c-afb9-7fa061561bf6)
And at only one point: 
![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2F5yHBfeIoss.png?alt=media&token=f4e79313-03ec-42f2-9739-248a2698905a)
There are only two exceptions to this property, and one is when a line is vertical, like so:
![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2Fbb4DUWX9X8.png?alt=media&token=8f4211e7-2866-41e4-8ab4-61b6ccff9a2a)
And the other is when the line is tangent to the curve, like this:
![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2Fka3rGZBn5b.png?alt=media&token=de4ca68c-8fa9-49ec-8a28-ea4a64072b37)
We'll be coming back to these two cases soon, but for now let's define point addition for the normal cases where the line intersects one or three points on the elliptic curve.
The way we do this is by finding the straight line that intersects the two points we want to add. Then we find the third point on the curve that the line intersects, reflect that point across the x-axis, and that point becomes our final result:
![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2FU1sYuV-9Ce.png?alt=media&token=2614ee5d-8417-4a8e-a72f-297988d26a3f)
While we can calculate point addition easily with a formula, intuitively, it does not make as much sense, as the addition of two points can result in a third point almost anywhere on the curve. While normal addition is a linear operation, point addition is highly nonlinear. This unpredictability is a property we're going to use later when we get to elliptic curve cryptography.
## **Point Addition Math**
Point addition is called addition because it shares four properties that we normally associate with addition: __identity__, __commutativity__, __associativity__, and __invertibility__.
__Identity__, here, like in the case of finite field addition, means that there is a zero. What this means practically for point addition is that there exists a point $$I$$, that when added to any point $$A$$, will result in $$A$$, just like when you add 0 to any integer, you get the integer back:
$$I + A = A$$
This concept of $$I$$ is related to the property of $$invertibility$$ that states that for any point $$A$$, there is another point $$-A$$ that results in the identity point $$I$$. The same way when you add a positive integer and its negative counterpart, you get 0:
$$A + (- A) = I$$
As you can see below, this is one of the special cases we discussed before where the line only intersects the elliptic curve at two points:
![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2Fbb4DUWX9X8.png?alt=media&token=8f4211e7-2866-41e4-8ab4-61b6ccff9a2a)
In the case of elliptic curves, we call the identity point the point at infinity. This is because there is a theoretical infinity point in the curve that will make the vertical line intersect the curve a third time.
__Commutativity__ means that the order of operation doesn't matter. So:
$$A + B = B + A$$
This can be seen easily in the diagrams, since the line going through $$A$$ and $$B$$ will result in the same third intersection point no matter whether you choose to add $$A$$ to $$B$$ or $$B$$ to $$A$$.
The final property, __associativity__ refers to this fact:
$$(A + B) + C = A + (B + C)$$
This means that the order of operations for compound operations also does not matter. While the graphs taken from __Programming Bitcoin__ below do not prove this property in a mathematical sense, it gives us confidence that it is true:
![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2FgqclJ8-oYf.png?alt=media&token=a2fd1579-3142-477d-91e2-b09011112516)
![](https://firebasestorage.googleapis.com/v0/b/firescript-577a2.appspot.com/o/imgs%2Fapp%2Fbardia%2FejfsloKuID.png?alt=media&token=514d28b2-fa11-42af-9e0b-942c8e6667ac)
## **Coding Point Addition**
Let's split up the coding point addition into three sections to handle the three different variations:
1. When the points are in a vertical line or when one of the points is the identity point
2. When the points are not in a vertical line and are different
3. When the points are equivalent
In Clojure(script), we can represent `Infinity` with the `##Inf` symbol. To get this working in Clojure, let's first make a change to our constructor function so it does not check the point at infinity's coordinates against the curve equation:
```(defn on-curve?
  "Checks if point is on elliptic curve"
  [x y a b]
  (= (int (** y 2)) (int (+ (** x 3) (* a x) b))))

(defn make-pt
  "Constructor function for elliptic curve points with validations"
  [x y a b]
  (if (and (= x ##Inf) (= y ##Inf))
    (Point. x y a b)
    (do
      (assert (on-curve? x y a b))
      (Point. x y a b))))```
Because our `Point` record is an extensible `Type` we can write polymorphic functions for, we can define the point addition operation, `+p` by defining a protocol to handle point addition called `PointOps`. Then we extend the `Point` type to support the new addition operation.
We'll first handle the cases for point addition where the points are in a vertical line or using the point at infinity:
```(defprotocol PointOps
  (+p [x y]))

(extend-type Point
  PointOps
  (+p [{x1 :x y1 :y a1 :a b1 :b, :as p1}
       {x2 :x y2 :y a2 :a b2 :b, :as p2}]
      (assert (and (= a1 a2) (= b1 b2)) "Points aren't on the same curve.")
      (cond
        (= x1 ##Inf) p2
        (= x2 ##Inf) p1
        (and (= x1 x2) (not= y1 y2)) (make-pt ##Inf ##Inf a1 b1))))```
We're deconstructing the arguments for the addition operation so we can more clearly access the record fields being passed through. And we assert the constants are the same so that we're adding points on the same curve.
When adding, if `x1` is `##Inf`, it means that `p1` is the point at infinity, so we return `p2`. and vice versa. And when we add two points that share the same $$x$$-coordinate but have different $$y$$-coordinates it means they are opposite the x-axis and the result of their point addition is the point at infinity.
## **Point Addition for when** $$x_1 \neq x_2$$
For points that have different $$x$$ values, we first have to find the slope created by the two points we want to add:
$$P_1 = (x_1, y_1), P_2 = (x_2, y_2), P_3 = (x_3, y_3)$$
$$P_1 + P_2 = P_3$$
$$s = (y_2-y_1)/(x_2-x_1)$$
And with the slope $$s$$, we can calculate $$x_3$$, and once we have $$x_3$$ we can calculate $$y_3$$, thus $$P_3$$ can be derived using this formula:
$$x_3 = s^2 - x_1 -x_2$$
$$y_3 = s(x_1 - x_3) - y_1$$
Let's go step-by-step through the process of point addition. So, for the curve $$y^2 = x^3 + 5x + 7$$, how do we find $$(2, 5) + (-1, -1)$$?
First, we plug in our coordinates into the formula for a slope of a line:
$$s = (-1 - 5)/(-1 -2) = -6/-3 = 2$$
Then we find $$x_3$$ using the formula we defined above:
$$x_3 = 2^2 - 2 - (-1) = 4 - 2 + 1 = 2 + 1 = 3$$
With $$x_3$$, we have what we need to find $$y3$$:
$$y_3 = 2(2 - 3) - 5 = 2(-1) - 5 = -2 - 5 = -7$$
And the final result:
$$P_3 = (3, -7)$$
## **Coding Point Addition for when** $$x_1 \neq x_2$$
Now let's code point addition for points with different $$x$$- and $$y$$-coordinates by adding another condition to our `+p` operator that implements the formulas we just discussed. We've extracted the slope formula in case we want to use it somewhere else.
```(defn slope
  "Calculates slope of a line"
  [x1 x2 y1 y2]
  (int (/ (- y2 y1) (- x2 x1))))

(extend-type Point
  PointOps
  (+p [{x1 :x y1 :y a1 :a b1 :b, :as p1}
       {x2 :x y2 :y a2 :a b2 :b, :as p2}]
      (assert (and (= a1 a2) (= b1 b2)) "Points aren't on the same curve.")
      (cond
        (= x1 ##Inf) p2
        (= x2 ##Inf) p1
        (and (= x1 x2) (not= y1 y2)) (make-pt ##Inf ##Inf a1 b1)
       	;; New code below
        (not= x1 x2) (let [s (slope x1 x2 y1 y2)
                           x3 (- (int (** s 2)) x1 x2)
                           y3 (-  (* s (- x1 x3)) y1)]
                       (make-pt x3 y3 a1 b1)))))```
## **Point Addition for when** $$P_1 = P_2$$
In order to calculate point addition for when the two points are equal, we have to calculate the line that's tangent to the curve at the two points, and then find the second point where that line intersects the curve.
Like before, we find the slope of the line, but this time, because we are trying to derive the instantaneous rate of change, we have to use calculus to derive the slope of the tangent line for the point being added to itself on the curve.
$$P_1 = (x_1, y_1) P_3 = (x_3, y_3)$$
$$P_1 + P_1 = P_3$$
We know that the slope at a given point is $$dy/dx$$ and to get this we need to take the derivative of both sides of the elliptic curve equation:
$$y^2 = x^3 + ax + b$$
$$2y dy = (3x^2 + a) dx$$
$$s = dy/dx = (3x^2 + a)/(2y)$$
The rest is the same as before, but we can combine $$x_1$$ and $$x_2$$ this time:
$$x_3 = s^2 - 2x_1$$
$$y_3 = s(x_1 - x_3) - y_1)$$
Now let's apply it to the $$y^2 = x^3 + 5x + 7$$ curve and find the answer of adding the point $$(-1, -1)$$ to itself.
First we find the slope:
$$s = (3(-1)^2 + 5)/(2(-1)) = (3 + 5)/(-2) = -4$$
Then $$x_3$$:
$$x_3 = (-4)^2 - 2(-1) = 16 + 2 = 18$$
Now $y_3$$:
$$y_3 = -4(-1 - 18) - (-1) =  -4(-19) + 1 = 77$$
And we have our result:
$$P_3 = (18, 77)$$
As can be seen here, adding the same point on an elliptic curve to itself can yield highly unintuitive results.
## **Coding Point Addition For When** $$P_1 = P_2$$
```(defn tangent-slope
  "Calculates the slope of a tangent line to the elliptic curve"
  [x y a]
  (int (/ (+ (* 3 (** x 2)) a) (* 2 y))))

(extend-type Point
  PointOps
  (+p [{x1 :x y1 :y a1 :a b1 :b, :as p1}
       {x2 :x y2 :y a2 :a b2 :b, :as p2}]
      (assert (and (= a1 a2) (= b1 b2)) "Points aren't on the same curve.")
      (cond
        (= x1 ##Inf) p2
        (= x2 ##Inf) p1
        (and (= x1 x2) (not= y1 y2)) (make-pt ##Inf ##Inf a1 b1)
        (not= x1 x2) (let [s (slope x1 x2 y1 y2)
                           x3 (- (int (** s 2)) x1 x2)
                           y3 (-  (* s (- x1 x3)) y1)]
                       (make-pt x3 y3 a1 b1))
        ;; New code below
        (and (= x1 x2) (= y1 y2)) (let [s (tangent-slope x1 y1 a1)
                                        x3 (- (int (** s 2)) x1 x2)
                                        y3 (- (* s (- x1 x3)) y1)]
                                    (make-pt x3 y3 a1 b1)))))```
Finally, we make a function to calculate the slope of the tangent line at a point on an elliptic curve and use it to calculate the result of point addition. Check in next week when we cover the combination of elliptic curves and finite fields to do elliptic curve cryptography—the basis of the signing and verification algorithms at the heart of Bitcoin transactions.
