(ns clevolution.app.parser
  (:require [instaparse.core :as insta]))


(def exp-parser
  (insta/parser
    "S = Sexp
    Sexp = term | '(' term (<whitespace> term)* ')'
    <term> = Keyword | Word | Number
    Keyword = ':' thing
    Word = thing
    Number = ratio | decimal | int
    <ratio> = int '/' int
    <decimal> = int '.' int
    Integer = int
    <thing> = #'[a-z.0-9-*+]+'
    <int> = #'[0-9]+'
    whitespace = #'\\s+'"))

(defn spans [t]
  (if (sequential? t)
    (cons (insta/span t) (map spans (next t)))
    t))