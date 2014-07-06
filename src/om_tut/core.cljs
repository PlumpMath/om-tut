(ns om-tut.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! >! close!]]))

(enable-console-print!)

(def out (chan))

(go (>! out "Channel Out working"))

(go
 (.log js/console (<! out)))

(defn random-output
  "Creates an output channel and prints a random number every n milliseconds"
  []
  (let [out (chan)]
     (js/setTimeout (fn [] (put! js/Date out)) (rand-nth (range 1000 4000))))
    out)

(defn listener
  "listens on a channel and prints to console"
  [c]
     (go
      (let [mesg (<! c)]
       (.log js/console mesg))))

;; changes to the atom (swap! or reset!) will trigger a re-render
;; (def app-state (atom {:text "Hello World!"}))

;(def app-state (atom {:list ["Lion" "Zebra" "Buffalo" "Antelope"]}))

(def app-state
  (atom
    {:contacts
     [{:first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}
      {:first "Alyssa" :middle-initial "P" :last "Hacker" :email "aphacker@mit.edu"}
      {:first "Eva" :middle "Lu" :last "Ator" :email "eval@mit.edu"}
      {:first "Louis" :last "Reasoner" :email "prolog@mit.edu"}
      {:first "Cy" :middle-initial "D" :last "Effect" :email "bugs@mit.edu"}
      {:first "Lem" :middle-initial "E" :last "Tweakit" :email "morebugs@mit.edu"}]}))

(defn middle-name [{:keys [middle middle-initial]}]
  (cond
    middle (str " " middle)
    middle-initial (str " " middle-initial ".")))

(defn display-name [{:keys [first last] :as contact}]
  (str last ", " first (middle-name contact)))

(defn contact-view [contact owner]
  (reify
    om/IRender
    (render [this]
      (dom/li nil (display-name contact)))))

(defn contacts-view [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
        (dom/h2 nil "Contact list")
        (apply dom/ul nil
          (om/build-all contact-view (:contacts app)))))))


(defn input-view [app owner]
  (reify
    om/IRender
    (render [this]
            (dom/div nil
               (dom/h2 nil "Input Test")
               (dom/input nil)
                     ))))

(defn main-view [app owner]
  (reify
    om/IRender
    (render [_]
            (dom/div nil
              (om/build contacts-view app)
              (om/build input-view nil)
                     ))))


;; om/root establishes rendering loop on specific element in DOM
;; it's safe to evaluate om/root multiple times
;; it takes 3 args:
;; 1) application state data and backing react component
;; 2) application state atom
;; 3) map with :target DOM node key-value pair.

(om/root main-view app-state
  {:target (. js/document (getElementById "main-view"))})

;; (om/root contacts-view app-state
;;   {:target (. js/document (getElementById "contacts"))})

;; (om/root input-view app-state
;;   {:target (. js/document (getElementById "input-view"))})


;; Test to see if there's a connection to the browser
(comment

  (.log js/console (str "Hello from lighttable " (js/Date.)))

  )



