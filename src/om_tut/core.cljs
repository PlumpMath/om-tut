(ns om-tut.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! >! close!]]
            [clojure.data :as data]
            [clojure.string :as string]))

(enable-console-print!)

(def out (chan))

(go (>! out "Channel Out Working"))

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
      {:first "Lem" :middle-initial "E" :last "Tweakit" :email "morebugs@mit.edu"}]
     :inc-value 0
     }))

;;; Foo

; How to get at the map in an atom (called deref)
(:contacts @app-state)

;; Output: [{:first "Ben", :last "Bitdiddle", :email "benb@mit.edu"} {:first "Alyssa", :middle-initial "P", :last "Hacker", :email "aphacker@mit.edu"} {:first "Eva", :middle "Lu", :last "Ator", :email "eval@mit.edu"} {:first "Louis", :last "Reasoner", :email "prolog@mit.edu"} {:first "Cy", :middle-initial "D", :last "Effect", :email "bugs@mit.edu"} {:first "Lem", :middle-initial "E", :last "Tweakit", :email "morebugs@mit.edu"}]

(def app-contacts (:contacts app-state))

(defn parse-contact [contact-str]
  (let [[first middle last :as parts] (string/split contact-str #"\s+")
        [first last middle] (if (nil? last) [first middle] [first last middle])
        middle (when middle (string/replace middle "." ""))
        c (if middle (count middle) 0)]
    (when (>= (count parts) 2)
      (cond-> {:first first :last last}
        (== c 1) (assoc :middle-initial middle)
        (>= c 2) (assoc :middle middle)))))


;; (parse-contact "Alex H. Eberts")

;; This won't work because it's not app-contacts is not a cursor at this point.
;; (om/transact! app-contacts :contacts #(conj % {:first "Alex" :last "Eberts" :email "alex@eberts.com"}))


(defn middle-name [{:keys [middle middle-initial]}]
  (cond
    middle (str " " middle)
    middle-initial (str " " middle-initial ".")))

(defn display-name [{:keys [first last] :as contact}]
  (str last ", " first (middle-name contact)))


(defn add-contact [app owner]
  (let [input (om/get-node owner "input-name")
        new-contact (-> input .-value parse-contact)]
;;    (.log js/console (str "new-contact: " new-contact))
    (when new-contact
      (om/transact! app #(conj % new-contact))
      (set! (.-value input) ""))))

(defn contact-view [contact owner]
  (reify
    om/IRender
    (render [this]
      (dom/li nil (display-name contact)))))

(defn contacts-view [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:id "contacts-view"}
        (dom/h2 nil "Contact list")
        (apply dom/ul nil
          (om/build-all contact-view (:contacts app)))))))

(defn input-view [app owner]
  (reify
    om/IRender
    (render [this]
            (dom/div #js {:id "input-view"}
               (dom/h2 nil "Input Test")
               (dom/input #js {:ref "input-name"})
                 (dom/button #js {:onClick #(add-contact app owner)} "Add")))))

(defn handle-inc [app owner]
  (om/transact! app :inc-value #(inc %)))

(defn main-view [app owner]
  (reify
    om/IRender
    (render [_]
            (dom/div #js {:id "parent-div"}
                     (dom/div nil
                              (om/build contacts-view app)
                              (om/build input-view (:contacts app)))
                     (dom/div #js {:id "incrementer"}
                              (dom/h2 nil (str "Current Count: " (:inc-value app)))
                              (dom/button #js {:onClick #(handle-inc app owner)} "Inc!"))))))


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



