(ns app.core
  (:require
    ["react" :as r]
    ["react-dom" :as rdom]
    ["react-hook-form" :as rhf]
    ["react-router-dom" :as rr]
    [app.lib :refer [defnc]]
    [applied-science.js-interop :as j]
    [superstring.core :as str]
    [clojure.zip :as z]
    [helix.core :as hx :refer [$ <>]]
    [helix.dom :as d]
    [helix.hooks :as hooks]
    [malli.core :as m]
    [malli.error :as me]
    [malli.util :as mu]
    ))

(def MalliSchema
  (m/schema
    [:map 
     ["person_name" string?]
     ["sex" 
      [:enum {:error/message "Should be: male | female"}
       "male" "female"]]]))

(defn rfh-validate
  [schema v]
  (or (m/validate schema v)
      (str/join \n (me/humanize
                     (m/explain schema v)))))

(defn nil-blank
  [v]
  (if (str/blank? v)
    nil
    v))

(defn use-form-state
  [initial schema]
  ;; not dealing with zipper to lower complexity, assuming schema points to a map
  {:pre [(m/schema? schema)
         (= :map (m/name schema))]}
  (j/let
    [[state set-state] (hooks/use-state initial)
     rfh-obj (rhf/useForm)
     m-entries (m/map-entries schema)]
    {:state state
     :set-state set-state
     :rfh-obj rfh-obj
     :map-entries m-entries}))

(defnc ExampleUseFormState
  []
  (j/let [{:keys [state set-state rfh-obj]} (use-form-state {} MalliSchema)
          ^:js {:keys [register
                       handleSubmit
                       errors] } rfh-obj

          fn-submit (fn [data event]
                      ;; we do not have data because state is on helix atom
                      (.log js/console "submitted from edn version")

                      )]

    (d/div
      (d/h3 "ExampleUseFormState")

      (d/form
        {:on-submit (handleSubmit fn-submit)}

        (d/div
          (d/label "person_name")
          (d/input {:ref (register "person_name"
                                   #js {:validate
                                        (fn [_] (rfh-validate 
                                                  (mu/get MalliSchema "person_name")
                                                  (get state "person_name")))
                                        })
                    :on-change
                    (fn [e]
                      (let [v (->  e .-target .-value nil-blank)]
                        (set-state assoc "person_name" v)
                        ))})
          ($ rhf/ErrorMessage {:name "person_name" :errors errors}))

        (d/div
          (d/label "sex")
          (d/input {
                    :ref (register "sex"
                                   #js {:validate
                                        (fn [_] (rfh-validate 
                                                  (mu/get MalliSchema "sex")
                                                  (get state "sex")))
                                        })
                    :on-change
                    (fn [e]
                      (let [v (->  e .-target .-value)]
                        (set-state assoc "sex" v)))})
          ($ rhf/ErrorMessage {:name "sex" :errors errors}) )

        (d/div
          (d/button {:type "submit"} "Submit"))

        (d/div (d/p "see console for errors object"))
        (.log js/console "errors edn version")
        (.log js/console errors)

        (d/div
          (d/p "state")
          (d/div (str state)))
        ))))

(defnc ExampleClojure
  []
  (j/let [[state set-state] (hooks/use-state {})
          ^:js {:keys [register
                       handleSubmit
                       errors] :as obj} (rhf/useForm)

          fn-submit (fn [data event]
                      ;; we do not have data because state is on helix atom
                      (.log js/console "submitted from edn version")

                      )]

    (d/div
      (d/h3 "Form data on edn")

      (d/form
        {:on-submit (handleSubmit fn-submit)}

        (d/div
          (d/label "person_name")
          (d/input {:ref (register "person_name"
                                   #js {:validate
                                        (fn [_] (rfh-validate 
                                                  (mu/get MalliSchema "person_name")
                                                  (get state "person_name")))
                                        })
                    :on-change
                    (fn [e]
                      (let [v (->  e .-target .-value nil-blank)]
                        (set-state assoc "person_name" v)
                        ))})
          ($ rhf/ErrorMessage {:name "person_name" :errors errors}))

        (d/div
          (d/label "sex")
          (d/input {
                    :ref (register "sex"
                                   #js {:validate
                                        (fn [_] (rfh-validate 
                                                  (mu/get MalliSchema "sex")
                                                  (get state "sex")))
                                        })
                    :on-change
                    (fn [e]
                      (let [v (->  e .-target .-value)]
                        (set-state assoc "sex" v)))})
          ($ rhf/ErrorMessage {:name "sex" :errors errors}) )

        (d/div
          (d/button {:type "submit"} "Submit"))

        (d/div (d/p "see console for errors object"))
        (.log js/console "errors edn version")
        (.log js/console errors)

        (d/div
          (d/p "state")
          (d/div (str state)))
        ))))


(defnc ExampleJavascript
  []
  (j/let [[state set-state] (hooks/use-state {} )
          ^:js {:keys [register
                       handleSubmit
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
        (.log js/console "errors js version")
        (.log js/console errors)

        (d/div
          (d/p "Values")
          (d/div (.stringify js/JSON (getValues))))
        ))))

(defn App
  []
  (d/div 
    ($ ExampleUseFormState)
    ($ ExampleClojure)
    ($ ExampleJavascript))
  )

(defn ^:export start
  []
  (rdom/render ($ App) (js/document.getElementById "app")))
