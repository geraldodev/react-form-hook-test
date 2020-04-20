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


(defnc App
  []
  (j/let [[state set-state] (hooks/use-state {} )
          ^:js {:keys [register
                       handleSubmit
                       watch
                       errors
                       setValue
                       getValues] :as obj} (rhf/useForm)

          ;; https://react-hook-form.com/api/#handleSubmit
          fn-submit (fn [data e]
                      (.log js/console "data on handleSubmit")
                      (.log js/console data)
                      (.log js/console "getValues on handleSubmit")
                      (.log js/console (getValues)))]

    (.log js/console obj)
    (d/form 
      {:on-submit (fn [e]
                    (.log js/console "on-submit")
                    (handleSubmit fn-submit)
                    (.preventDefault e)
                    (.log js/console "errors:")
                    (.log js/console errors)
                    (.log js/console "getValues():")
                    (.log js/console (getValues))
                    )}
      (d/div
        (.createElement r "input"
                        #js {:ref (register #js {:name "foo"}
                                            #js {:required true})
                             :name "foo"
                             :defaultValue "hey"
                             :onChange 
                             (fn [e]
                               (let [v (->  e .-target .-value)]
                                 (.log js/console (str "setting value " v))
                                 (setValue "foo" v)) )}))
      (d/div 
        (d/input {:name "teste" 
                  :ref (register #js {:required true})}))
      (d/div
        (d/button {:type "submit"} "Submit"))
        )))


(defn ^:export start
  []
  (rdom/render ($ App) (js/document.getElementById "app")))
