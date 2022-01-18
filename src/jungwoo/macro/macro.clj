(ns jungwoo.macro.macro)

'+
(+ 1 2)

'(+ 1 2)
`+
(quote (+ 1 2))

`(+ 1 2)
`~(+ 1 2)
'(1 2 3)

(list 1 2 3)

'(list 1 2 3) ; ' This tells Clojure to turn off evaluation for whatever follows

`(list 1 2 3)

`(~list 1 2 3)

`(~list `list 'list ~'list 1 2 3)
`(+ 1 ~(inc 1))
`(+ 1 (~inc 1))
`(+ 1 ~(inc 1))
`(+ 1 (inc 1))

`(+ ~(list 1 2 3) 2)
`(+ ~@(list 1 2 3) 2)
`(+ (list 1 2 3))

(gensym 'foo)
`foo#
`foo#

'+

`+

(def a 1)

(symbol? a)

(symbol? 'a)

(let [a (+ 1 1)]
  `(quote ~a))

(symbol? 1)
(symbol? '1)
(def c (symbol "foo"))
c
(def b (list 1 2 3))

(symbol b)

;; (defmacro test-macro [name]
;;   (symbol))

(let [a (if true (do) (do))]
  (prn a))

(gensym 'ctx_)


(defn criticize-code
  [criticism code]
  `(println ~criticism (quote ~code)))

(defmacro code-critic
  [bad good]
  `(do ~(criticize-code "Cursed bacteria of Liberia, this is bad code:" bad)
       ~(criticize-code "Sweet sacred boa of Western and Eastern Samoa, this is good code:" good)))

(defmacro code-critic
  [bad good]
  `(do ~(map #(apply criticize-code %)
             [["Great squid of Madrid, this is bad code:" bad]
              ["Sweet gorilla of Manila, this is good code:" good]])))

;; (code-critic (1 + 1) (+ 1 1))

(+ 1 1)

(clojure.core/println "criticism" '(1 + 1))

(def message "Good job!")
(defmacro with-mischief
  [& stuff-to-do]
  (concat (list 'let ['message "Oh, big deal!"])
          stuff-to-do))

(if true 1 2)
(with-mischief
  (println "Here's how I feel about that thing you did: " message))

(macroexpand-1 '(with-mischief
                  (println "Here's how I feel about that thing you did: " message)))

(defmacro without-mischief
  [& stuff-to-do]
  `(let [message# "Oh, big deal!"]
     ~@stuff-to-do
     (println "I still need to say: " message#)))


(without-mischief
 (println "Here's how I feel about that thing you did: " message))
; Exception: Can't let qualified name: user/message

`(let [name# "Larry Potter"] name#)


(def ^Integer five 5)

(def ^String five-str "5")


(meta #'five-str)
(meta #'five)

(+ 1 five-str)
