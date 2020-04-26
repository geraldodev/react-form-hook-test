(ns user
  (:require
    [app.malli-zipper :as mz :refer [malli-zipper ]]
    [clojure.pprint :refer [pprint]]
    [clojure.tools.namespace.repl :as tns :refer [set-refresh-dirs]]
    [clojure.zip :as z]
    [malli.core :as m]
    [malli.error :as me]
    [malli.util :as mu]
    [spy.core :as spy]
    ))

(set-refresh-dirs "src/main" "src/dev")
;; Change the default output of spec to be more readable
; (alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn restart
  "Stop, reload code, and restart the server. If there is a compile error, use:

  ```
  (tns/refresh)
  ```

  to recompile, and then use `start` once things are good."
  []
  (tns/refresh ))

(defn r
  []
  (restart)
  )


(defn ride-malli-zipper
  [schema]
  (loop [loc (malli-zipper schema)]
    (if (z/end? loc)
      (println "end")
      (let [n (z/node loc)
            n (mz/malli-tag n)]
        (println n)
        (recur (z/next loc) )))))

 (def Schema
   (m/schema
     [:and
      [:map
       {:foo :bar}
       [:nome string?]
       [:sexo string? [:enum "masculino" "feminino"]]
       [:endereco [:maybe [:map
                           [:cep string?]
                           [:estado string?]
                           [:telefone {:optional true} string?]]] ]
       ]
      [:fn 'identity]
      ]))

(comment
  (pprint (mz/just-map-schema Schema))
  (spy/calls mz/spy-some-map)
  (identity mz/spy-some-map)

  (mz/some-map Schema)

  (print *e)
  (m/name nil)
  (m/children [:map [:foo string?]])
  (-> Schema malli-zipper z/node m/name)
  (ride-malli-zipper Schema)
  (seq? "")
  (satisfies? m/Schema (m/name nil))
  (m/children int?)

  (m/children [:maybe int?])
  (-> Schema m/children first m/children first )
  (-> Schema malli-zipper z/next)
  (-> Schema malli-zipper z/next z/next z/next z/next )
  (m/children Schema)
  (mu/get [:and {:default 3} int?] :and) ;; get nao funciona com and
  (mu/get [:map [:nome string?]] :map) ;; get nao funciona com map
  (mu/get [:map [:nome string?]] :nome) ;; get pega  a definicao de item nomeado
  (m/name Schema) ;; name é o tipo, (nth 0)
  (m/name [:map [:nome string?]]) ;; :map
  (m/properties [:and {:foo :bar} int?]) ;; properties sao as informacoes adicionais (nth 1)
  (m/name (mu/get Schema :and))
  (m/map-entries Schema)

  (m/form Schema)
  (m/children Schema)

  (pprint
    (m/accept
      Schema
      m/map-syntax-visitor))
  (m/properties Schema)
  (mu/select-keys Schema  [:nome :cep  ])
  (mu/get Schema :nome)
  (mu/get-in Schema [:endereco :cep])
  (mu/get-in Schema [:endereco])
  (mu/get Schema :map) ;; map nao funciona faz parte da dsl
  (m/validate string? "teste")
  (m/validate string? 1)

  (m/children [:and string?] )
  (m/children [:and string?] )
  (m/children [:map [:nome string?]] )

(m/schema [:and
     [:map
      [:x int?]
      [:y int?]]
     [:fn '(fn [{:keys [x y]}] (> x y))]])

;; aparentemente dentro do :and fn pode vir antes de map
(m/schema
  [:and
   [:fn '(fn [{:keys [x y]}] (> x y))]
   [:map
    [:x int?]
    [:y int?]]
   ])

(m/map-entries [:map [:nome string?]])
(= (m/children [:map [:nome string?]])
   (m/map-entries [:map [:nome string?]]))

(m/name [:maybe integer?])

(print *e)
  )
