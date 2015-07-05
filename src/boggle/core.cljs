;=======================================================================================================
(ns ^:figwheel-always boggle.core
    (:require [clojure.browser.repl :as repl]
            [figwheel.client :as fw]
            [sablono.core :as html :refer-macros [html]]
            [quiescent :as q :include-macros true]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")


;=======================================================================================================
;(defonce world (atom {:list ["cat" "dog" "penguin" "albatross"] :index 0 :words {}}))
(def world (atom {:list ["cat" "dog" "penguin" "albatross"] :index 0 :words {}}))

(defn on-click [e]
	;(swap! world #(cons "kangaroo" %))
	(swap! world assoc-in [:list] (cons "kangaroo" (@world :list))))

(defn on-key-down [e]
	(println "pressed"))

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


;=======================================================================================================
(def board
  [
    ["d" "i" "n" "g" "e"]
    ["d" "z" "g" "i" "r"]
    ["a" "p" "l" "b" "o"]
    ["y" "b" "c" "d" "e"]
    ["z" "e" "k" "l" "q"]])

(defn trie-add [trie word]
	"usage (trie-add {} \"catastrophe\")"
	(defn trie-add' [[letter & remaining] trie]
		(if (empty? remaining)
			(if (trie letter)
				(assoc trie letter (assoc (trie letter) word 1))
				(assoc trie letter {word 1}))
			(let [next (trie letter {})]
				(assoc trie letter (trie-add' remaining next)))))
	(trie-add' word trie))

(defonce words-trie (reduce trie-add {} (js->clj (aget js/window "words"))))

(defn next-spots [[x y] prev]
	"usage: (next-spots [2 2] [[1 1] [1 2] [1 3]])"
	(->>	
		[
			[x (dec y)]
			[(inc x) (dec y)]
			[(inc x) y]
			[(inc x) (inc y)]
			[x (inc y)]
			[(dec x) (inc y)]
			[(dec x) y]
			[(dec x) (dec y)]]
		(filter
			(fn [[x y]]
				(if (or (< x 0) (< y 0) (> x 4) (> y 4)) false true)))
		(filter
			(fn [xy]
			 	(if (some #(= xy %) prev) false true)))
		(into [])))

(defn print-word [trie]
	"usage: (print-word words-trie)"
	(let [w (filter #(> (count %) 1) (keys trie))]
		(if (not (empty? w)) 
			(println "word:" (first w)))))

(defn print-words-from [board words [X Y :as XY] prev]
	"usage: (print-words-from board words-trie [2 0] [])"
	(let [letter ((board Y) X)]
		(if (words letter)
			(do 
				(print-word (words letter))
				(doseq [xy (next-spots [X Y] prev)]
					(print-words-from board (words letter) xy (cons XY prev)))))))

;=======================================================================================================
(.clear js/console)
(println "=====================")
(doseq 
	[xy (for [x (range 5) y (range 5)] [x y])]
		(print-words-from board words-trie xy []))
