(ns todo-mvc.core
  (:require
    [applied-science.js-interop :as j]
    [clojure.string :as string]
    [helix.core :as hx :refer [$ <>]]
    [helix.dom :as d]
    [helix.hooks :as hooks]
    [todo-mvc.lib :refer [defnc]]
    ["react" :as r]
    ["react-dom" :as rdom]
    ["react-router-dom" :as rr]
    ["react-hook-form" :as rhf]))

(defnc ExampleClojure
  []
  (j/let [[state set-state] (hooks/use-state {} )
          ^:js {:keys [register
                       handleSubmit
                       watch
                       errors
                       setValue
                       getValues] :as obj} (rhf/useForm)

          ;; https://react-hook-form.com/api/#handleSubmit
          ;; this is just called if the form has no error
          fn-submit (fn [data event]
                      (.log js/console "data on handleSubmit")
                      (.log js/console data)

                      (.log js/console "getValues on handleSubmit")
                      (.log js/console (getValues))
                      )]

    (d/div
      (d/h3 "Form data on edn")

      (d/form
        {:on-submit (handleSubmit fn-submit)}

        (d/div
          (d/label "person_name")
          (d/input {; option 1
                    ; setting input's :name and calling register on :ref are enough to
                    ; to register the input with react-form-hook
                    ; like this :
                    ; :name "person_name"
                    ; :ref (register #js {:required "person's name is required"})

                    ; option 2
                    ; the alternative is specify the name on the register's call
                    ; and invoke setValue in a on-change handler
                    :ref (register "person_name" ; <- notice the name of the field
                                   #js {:required "person's name is required"})
                    :on-change
                    (fn [e]
                      (let [v (->  e .-target .-value)]
                        (.log js/console (str "setting value " v))
                        (setValue "person_name" v)) )
                    })
          ($ rhf/ErrorMessage {:name "person_name" :errors errors}))

        (d/div
          (d/label "sex")
          (d/input {:name "sex"
                    :ref (register
                           #js {:validate
                                #js {:male_female
                                     ;; we have to coerce the validation because a string result is considered an error message, by coercing when returning true it is considered a valid value
                                     (fn [v] (or (boolean
                                                   (#{"male" "female"} v ))
                                                 "Should be male or female"))}
                                })})
          ($ rhf/ErrorMessage {:name "sex" :errors errors}) )

        (d/div
          (d/button {:type "submit"} "Submit"))

        (d/div (d/p "see console for errors object"))
        (.log js/console "errors")
        (.log js/console errors)

        (d/div
          (d/p "Values")
          (d/div (.stringify js/JSON (getValues))))
        ))))
(defnc ExampleJavascript
  []
  (j/let [[state set-state] (hooks/use-state {} )
          ^:js {:keys [register
                       handleSubmit
                       watch
                       errors
                       setValue
                       getValues] :as obj} (rhf/useForm)

          ;; https://react-hook-form.com/api/#handleSubmit
          ;; this is just called if the form has no error
          fn-submit (fn [data event]
                      (.log js/console "data on handleSubmit")
                      (.log js/console data)

                      (.log js/console "getValues on handleSubmit")
                      (.log js/console (getValues))
                      )]

    (d/div
      (d/h3 "Form data on javascript Object")

      (d/form
        {:on-submit (handleSubmit fn-submit)}

        (d/div
          (d/label "person_name")
          (d/input {; option 1
                    ; setting input's :name and calling register on :ref are enough to
                    ; to register the input with react-form-hook
                    ; like this :
                    ; :name "person_name"
                    ; :ref (register #js {:required "person's name is required"})

                    ; option 2
                    ; the alternative is specify the name on the register's call
                    ; and invoke setValue in a on-change handler
                    :ref (register "person_name" ; <- notice the name of the field
                                   #js {:required "person's name is required"})
                    :on-change
                    (fn [e]
                      (let [v (->  e .-target .-value)]
                        (.log js/console (str "setting value " v))
                        (setValue "person_name" v)) )
                    })
          ($ rhf/ErrorMessage {:name "person_name" :errors errors}))

        (d/div
          (d/label "sex")
          (d/input {:name "sex"
                    :ref (register
                           #js {:validate
                                #js {:male_female
                                     ;; we have to coerce the validation because a string result is considered an error message, by coercing when returning true it is considered a valid value
                                     (fn [v] (or (boolean
                                                   (#{"male" "female"} v ))
                                                 "Should be male or female"))}
                                })})
          ($ rhf/ErrorMessage {:name "sex" :errors errors}) )

        (d/div
          (d/button {:type "submit"} "Submit"))

        (d/div (d/p "see console for errors object"))
        (.log js/console "errors")
        (.log js/console errors)

        (d/div
          (d/p "Values")
          (d/div (.stringify js/JSON (getValues))))
        ))))

(defn App
  []
  (d/div 
    ($ ExampleClojure)
    ($ ExampleJavascript))
  )

(defn ^:export start
  []
  (rdom/render ($ App) (js/document.getElementById "app")))
