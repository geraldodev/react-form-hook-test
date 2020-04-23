(ns app.malli-zipper
  (:require
    [clojure.zip :as z]
    [malli.core :as m]
    [malli.util :as mu]
    [malli.error :as me]
    ))

(defn make-malli-node
  [n c]
  (m/into-schema (m/name n) (m/properties n) c))

(defn have-children?
  [s]
  (when (vector? s)
    (when-let [tag (first s)]
      (when (get m/base-registry tag)
        true))))

(defn safe-children
  [s]
  (try
    (m/children s)
    (catch Exception _ 
      (if (and (vector? s)
               (= 2 (count s))
               (have-children? (second s)))
        (do
          (prn "map item with children " (first s))
          (second s))
        (do 
          (prn "no children for " s)
          nil)))))

(defn malli-branch?
  [x]
  (if (and (vector? x)
           (= 2 (count x)))
    true ;; can be a map item
    (safe-children x)))

(defn malli-zipper
 [root]
 (z/zipper malli-branch? safe-children make-malli-node root))

(defn some-map
  [x]
  (let [n (m/name x)]
    (cond (= :and n)
          (some->> (m/children x)
                   (some #(when (= :map (m/name %))
                            %)))
          (= :map n)
          x
          )))

#_(defn just-map-schema
  [s]
  (let [i (malli-zipper s)]
    (loop [n (z/node i)]
      (prn (m/name n))
      (let [n (z/next n)]
        (if n
          (recur ))))))

