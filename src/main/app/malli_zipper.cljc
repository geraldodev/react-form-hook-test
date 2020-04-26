(ns app.malli-zipper
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.zip :as z]
    [malli.core :as m]
    [malli.error :as me]
    [malli.util :as mu]
    [spy.core :as spy]
    ))

(defn make-malli-node
  [n c]
  ; (prn "make malli node n type :" (type n) " n -> " n)
  ; (prn "make malli node c type :" (type c) " c -> " c)
  ;; IMPOSSIBLE because the way the zipper is configured replacing map items would generate
  ;; (m/name [:endereco [:maybe [:map {:1 :2} [:telefone {:optional true} string?]]]])
  ;; map items are not valid schemas
  (m/into-schema (m/name n) (m/properties n) c))

(defn malli-tag
  [x]
  (cond (m/schema? x)
        (m/name x)

        (and (vector? x))
        (first x)))

(defn have-children?
  [s]
  (when (get m/base-registry (malli-tag s))
    true))

(defn malli-map-entry?
  [x]
  (and (vector? x)
       (<= 2 (count x) 3)))

(defn safe-children
  [s]
  (try
    ; (prn "safe-children com " s)
    (m/children s)
    (catch Exception _
      (if (and (malli-map-entry? s)
               (have-children? (last s)))
        (do
          ; (prn "map item with children " (first s) " children " (last s))
          (list (last s)))
        (do
          ; (prn "no children for " s)
          nil)))))


(defn malli-branch?
  [s]
  (let [b (cond (m/schema? s)
                (boolean (m/children s))

                (vector? s)
                (cond (get m/base-registry (first s))
                      ;; [:maybe blabla]
                      (boolean (m/children (m/schema s)))

                      (and (<= 2 (count s) 3)
                           (have-children? (last s)))
                      ;; [:mapentry {someproperty} children]
                      true)
                )]
    ; (prn "branch? " s " result " b " type " (type s))
    b)

  #_(try
    (m/children s)
    (catch Exception _
      (if (and (malli-map-entry? s)
               (have-children? (last s)))
        (do
          (prn "branch item with children " (first s) " children " (last s))
          (last s))
        (do
          (prn "no branch   " s)
          nil))))
  )

(defn malli-zipper
 [root]
 ; (z/zipper malli-branch? safe-children make-malli-node root)
 (z/zipper #(or (vector? %)
                (m/schema? %)) safe-children make-malli-node root)
 )

(defn some-map
  [x]
  ; (println "chamou some-map")
  (some->> (m/children x)
           (some #(when (= :map (m/name %))
                    %))))

(def spy-some-map (spy/spy some-map))

(def outer-tags-to-favor-map #{:and :maybe})

(defn just-map-schema
  [s]
  (loop [loc (malli-zipper s)]
    (let [n (z/node loc)
          ; _ (println "n type " (type n) " -> " n)
          map-node-to-replace-outer-tag (and (get outer-tags-to-favor-map (malli-tag n))
                                             (some-map n))]
      (if map-node-to-replace-outer-tag
        (do
          
          ;; TODO if n the node of the outer tag is not a valid schema
          ;; it probably is a map item . z/up could be used to locate the map above
          ;; and replace the map as a whole
          (if (or (nil? (z/up loc))
                  (get m/base-registry (malli-tag (z/node (z/up loc)))))
            (do
              (println "replaced !")
              (recur (z/replace loc map-node-to-replace-outer-tag))) 
            (do 
              (println "cannot replace prolly because is a map item (recursive operation up to the top map)")
              (recur (z/next loc))))
          )
        (let [loc (z/next loc)]
          (if (z/end? loc)
            (z/node loc)
            (recur loc)))))))

(defn ride-malli-zipper
  [schema]
  (loop [loc (malli-zipper schema)]
    (if (z/end? loc)
      ; (println "end")
      (let [n (z/node loc)
            n (malli-tag n)]
        (println n)
        (recur (z/next loc) )))))

(def Schema
  #_(m/schema)

  [:and
   [:map
    {:foo :bar}
    [:nome string?]
    [:endereco (m/schema  [:maybe (m/schema [:map {:1 :2}
                                             [:telefone {:optional true} string?]])]) ]
    ]  
   [:fn 'identity]
   ])

; [:map {:in []}
;  [:id [string? {:in [:id]}]]
;  [:tags
;   [:set {:in [:tags]}
;    [keyword? {:in [:tags :malli.core/in]}]]]
;  [:address
;   [:maybe {:in [:address]}
;    [:vector {:in [:address]}
;     [:map {:in [:address :malli.core/in]} 
;      [:street [string? {:in [:address :malli.core/in :street]}]]
;      [:lonlat
;       [:tuple {:in [:address :malli.core/in :lonlat]}
;        [double? {:in [:address :malli.core/in :lonlat 0]}]
;        [double? {:in [:address :malli.core/in :lonlat 1]}]]]]]]]]

(defn in-example 
  []
  (-> 
    (m/accept
      [:map
       [:id string?]
       [:tags [:set keyword?]]
       [:address
        [:maybe
         [:vector
          [:map
           [:street string?]
           [:lonlat [:tuple double? double?]]]]]]]
      (fn [schema children in options]
        (m/into-schema
          (m/name schema)
          (assoc (m/properties schema) :in in)
          children
          options)))
    pprint))

(comment

  (me/humanize
    (m/explain
       [:and  string? [:not= {:error/message "não pode ser vazio"} ""] ]
       1))

  (me/humanize (m/explain [:map [:foo {:optional true} string?]] {}))

  (me/humanize (m/explain [:enum "male" "female"] nil))
  (m/validate string? 1)
  (me/humanize (m/explain string? 1))
  (in-example)

  (m/form (m/schema Schema)) 

  (mu/get [:map [:nome string?]] :nome)
  
  (m/name  (m/schema [:endereco [:maybe [:map {:1 :2} [:telefone {:optional true} string?]]]]))
  (m/into-schema string? {} nil)

  ; "make malli node c type :" clojure.lang.LazySeq " c -> " ([:map {:1 :2} [:telefone {:optional true} string?]])

  (print *e)

  (m/children [:map {:foo :bar} [:1 string?]])
  (ride-malli-zipper Schema)
  (just-map-schema Schema)
  (m/children [:map [:foo string?]])

  ; "make malli node c type :" clojure.lang.LazySeq " c -> " ([:map {:1 :2} [:telefone {:optional true} string?]])
  ; (m/into-schema (m/name [:endereco [:maybe [:map {:1 :2} [:telefone {:optional true} string?]]]]) (m/properties n) c)
  (m/schema? (m/schema string?))
  (m/name [:map [:foo string?]])

  (m/schema? nil)

  )

