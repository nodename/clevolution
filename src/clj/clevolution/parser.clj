(ns clevolution.parser
  (:require [instaparse.core :as insta]))

(def exp-parser
  (insta/parser
    "S = Sexp | term
    Sexp =  <whitespace>* <'('> Word (<whitespace> (Sexp | term))* <')'> <whitespace>*
    <term> = Key-Value | Word | Number | Color | Matrix3x3
    Key-Value = Keyword <whitespace> (Number | Color)
    Keyword = ':' thing
    Word = thing
    Color = <'['> Number <whitespace> Number (<whitespace> Number)+ <']'>
    Matrix3x3 = <'['> Vec3 <whitespace> Vec3 <whitespace> Vec3 <']'>
    Vec3 = <'['> Number <whitespace> Number <whitespace> Number <']'>
    Number = ratio | decimal | int | scientific
    <ratio> = int '/' int
    <scientific> = decimal 'E' #'-?' int
    <decimal> = int '.' int
    Integer = int
    <thing> = #'[a-z.0-9-*+]+'
    <int> = #'[0-9]+'
    whitespace = #'\\s+'"))

(defn spans [t]
  (if (sequential? t)
    (cons (insta/span t) (map spans (next t)))
    t))