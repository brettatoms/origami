(ns opencv4.filter
  (:require
    [clojure.java.data :as j]))

(defn s->filter
  [values]
  (cond
    (instance? java.io.File values)
    (s->filter (slurp values))
    (string? values)
    (let [ r (read-string values) ] (s->filter r))
    (map? values)
    (j/to-java (eval (:class values)) values)
    (coll? values)
     (into-array origami.Filter (map s->filter values))
     :else nil))

(defn filter->s
  ([values]
   (cond
     (string? values)
     (filter->s (read-string values))
     (coll? values)
     (apply str (map bean values))
     (map? values)
     (str (bean values))
     :else
     nil))
  ([values filename]
   (spit filename filter->s values)))

(defn- to-filter-fn[_fn]
  (fn [mat] (.apply _fn mat)))
(defn s->fn-filter [values]
  (let [_fn (s->filter values)]
    (cond
      (coll? _fn)
      (apply comp (map to-filter-fn _fn)); apply many
      :else
      (to-filter-fn _fn))))
