;=======================================================================================================
;(defn & [x y] (and x y))
;(defn | [x y] (or x y))

;(defmacro def! [x n]
;  `(def ~x (atom ~n)))

;(defmacro |> [first & rest]
;    (reduce (fn [prev# next#] (reverse (cons prev# (if (list? next#) (reverse next#) (list next#)))) ) first rest))


;=======================================================================================================

(ns ^:figwheel-always boggle.core
    (:require [clojure.browser.repl :as repl]
            [figwheel.client :as fw]
            [sablono.core :as html :refer-macros [html]]
            [quiescent :as q :include-macros true]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

;(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

 
; (->> (range)
; 	(map #(* % %))
; 	(filter even?)
; 	(take 10)
; 	(reduce +))


;; ===============================================

;; (repl/connect "http://localhost:9000/repl")

;(defonce world (atom ["cat" "dog" "penguin"]))
(def world (atom {:list ["cat" "dog" "penguin" "albatross"] :index 0 :words {}}))

;(swap! my-atom assoc-in [:map :new-key] value)
(defn on-click [e]
	;(swap! world #(cons "kangaroo" %))
	(swap! world assoc-in [:list] (cons "kangaroo" (@world :list)))
)
(defn on-key-down [e]
	(println "pressed")
)

(q/defcomponent Root
  [data]
  (html
    [:div {
		;NOTE: on-key-down on a <div> does not appear to work (at least not like this).
    	:on-key-down #(on-key-down %1)
    }
      [:h1 (:text data)]
	  [:button {
        	:on-click #(on-click %1)
    	} 
        "push"]
      [:ul
      	(map (fn [d] [:li d]) (data :list))
      ]
    ]))

(defn render [data]
  (q/render (Root data)
    (.getElementById js/document "app")))


(add-watch world ::render
    (fn [_ _ _ data] (render data)))


(render @world)


'(fw/watch-and-reload :jsload-callback
  (fn [] (swap! world update-in [:tmp-dev] not)))


;=========================================================

(def found (atom {
  "padding" true
  "pad" true
  "led" true
  "ding" true}))

(def board
  [
    ["d" "i" "n" "g" "e"]
    ["d" "z" "g" "i" "r"]
    ["a" "p" "l" "b" "o"]
    ["y" "b" "c" "d" "e"]
    ["z" "e" "k" "l" "q"]])

(defn trie-add [trie fullword]
	;TODO: overloaded function that only takes fullword and inserts empty {} for trie.
	(defn trie-addo [word trie]
		;TODO: word can not be an empty? list.
		(def letter (first word))
		(def remaining (rest word))
		;TODO: the above 2 variables should be a let.
		(if (empty? remaining)
			(if (contains? trie letter)
				(assoc trie fullword 1)
				(assoc trie letter {fullword 1})
			)
			(if (contains? trie letter)
				(assoc trie letter (trie-addo remaining (trie letter)))
				(assoc trie letter (trie-addo remaining {}))
			)
		)
	)
	(trie-addo fullword trie)
)
;testing trie-add:
;(println (trie-add (trie-add (trie-add (trie-add {} "pads") "zoo") "zip") "pad"))
;(println (trie-add (trie-add {} "po") "pod"))

(defonce words-trie (reduce trie-add {} (js->clj (aget js/window "words"))))

;next possible boggle moves based on 5x5 board and each dice only used once per word.
(defn next-spots [x y prev]
	(filter
		(fn [xy]
		 	(if (some #(= xy %) prev) false true))
		(filter
			(fn [[x y]]
				(if (or (= x -1) (= y -1) (= x 5) (= y 5)) false true))
			[
				[x (dec y)]
				[(inc x) (dec y)]
				[(inc x) y]
				[(inc x) (inc y)]
				[x (inc y)]
				[(dec x) (inc y)]
				[(dec x) y]
				[(dec x) (dec y)]
			])))
;testing next-spots"
;(println (next-spots 0 0 [[0 1] [1 0]]))

(defn words? [trie]
	(filter #(> (count %) 1) (keys trie))
)
;testing words?:
;(println (words? (((words-trie "z") "o") "o") ))

(defn find-from [board words [X Y :as XY] prev]
	(let [l ((board Y) X)]
		;(println l (map (fn [[x y]] ((board y) x) ) prev))
		;(println (next-spots X Y prev))
		(if (contains? words l)
			(do 
				;(println l (map (fn [[x y]] ((board y) x) ) prev))
				(if (not (empty? (words? (words l)))) (println "word:" (first (words? (words l)))))
				(doseq [xy (next-spots X Y prev)]
					;(println X Y "next:" xy)
					;(println l (map (fn [[x y]] ((board y) x) ) prev))
					;(if (not (empty? (words? (words l)))) (println "words:" (words? (words l))))
					(find-from board (words l) xy (concat [XY] prev))
				)
			)

			;(next-spots x y [])
			;(find-from board (words l) (+ x 1) (+ y 1)) ; x + 1, y - y, etc...  
			;also, on this line check for complete words and add to found atom.
	))
)

;|>

(.clear js/console)
(println "=====================")
(doseq 
	[xy (for [x (range 5) y (range 5)] [x y])]
	(find-from board words-trie xy [])
)
;(println (find-from board words-trie [0 0] []))




;(println ((words-trie "d") "d"))


; (let [[a b & the-rest] my-vector]
;   (println a b the-rest))
; ;; => :a :b (:c :d)
