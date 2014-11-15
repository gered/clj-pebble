(ns clj-pebble.standard-extensions
  (:import (java.lang Math)
           (org.apache.commons.lang3.text WordUtils)
           (org.apache.commons.lang3 StringUtils))
  (:require [clojure.pprint]
            [clojure.string :as string]
            [cheshire.core :as json]))

(def extensions
  {:functions
   {"concat"
    (fn [& values]
      (apply str values))

    "dump"
    (fn [x]
      (with-out-str
        (clojure.pprint/pprint x)))

    "dump_table"
    (fn [x]
      (with-out-str
        (clojure.pprint/print-table x)))

    "format"
    format

    "random"
    (fn [& values]
      (let [first-value (first values)]
        (cond
          (and (= (count values) 1)
               (coll? first-value))
          (rand-nth first-value)

          (> (count values) 1)
          (rand-nth values)

          (string? first-value)
          (rand-nth (seq first-value))

          (number? first-value)
          (rand-int first-value)

          :else
          (rand))))

    "range"
    (fn [low high & [step]]
      (range low high (or step 1)))

    "repeat"
    (fn [s count]
      (StringUtils/repeat s count))
    }

   :filters
   {"abs"
    (fn [n]
      (Math/abs n))

    "batch"
    (fn [coll size & [filler]]
      (partition size size (or filler nil) coll))

    "butlast"
    butlast

    "capitalize"
    (fn [s]
      (WordUtils/capitalize s))

    "ceil"
    (fn [n]
      (Math/ceil n))

    "center"
    (fn [s max-width & [padding-string]]
      (StringUtils/center s max-width (or padding-string \space)))

    "contains"
    (fn [haystack needle]
      (cond
        (map? haystack) (contains? haystack needle)
        (string? haystack) (.contains haystack needle)
        ; note: explicit use of '=' to allow testing for falsey values
        (coll? haystack) (not (nil? (some #(= needle %) haystack)))
        :else (throw (new Exception (str "'contains' passed invalid collection type: " (type haystack))))))

    "first"
    first

    "floor"
    (fn [n]
      (Math/floor n))

    "index_of"
    (fn [haystack needle]
      (cond
        (instance? java.util.List haystack) (.indexOf haystack needle)
        (string? haystack) (.indexOf haystack (if (char? needle) (int needle) needle))
        :else (throw (new Exception (str "'index_of' passed invalid collection type: " (type haystack))))))

    "join"
    (fn [coll & [separator]]
      (if separator
        (string/join separator coll)
        (string/join coll)))

    "json_encode"
    (fn [x & [pretty-print?]]
      (json/generate-string x (if pretty-print? {:pretty true})))

    "keys"
    keys

    "last"
    last

    "last_index_of"
    (fn [haystack needle]
      (cond
        (instance? java.util.List haystack) (.lastIndexOf haystack needle)
        (string? haystack) (.lastIndexOf haystack (if (char? needle) (int needle) needle))
        :else (throw (new Exception (str "'last_index_of' passed invalid collection type: " (type haystack))))))

    "max"
    (fn [coll]
      (apply max coll))

    "min"
    (fn [coll]
      (apply min coll))

    "nl2br"
    (fn [s]
      (.replace s "\n" "<br />"))

    "normalize_space"
    (fn [s]
      (StringUtils/normalizeSpace s))

    "nth"
    (fn [coll idx & [default]]
      (if default
        (nth coll idx default)
        (nth coll idx)))

    "pad_left"
    (fn [s max-width & [padding-char]]
      (StringUtils/leftPad s max-width (or padding-char \space)))

    "pad_right"
    (fn [s max-width & [padding-char]]
      (StringUtils/rightPad s max-width (or padding-char \space)))

    "replace"
    (fn [s & replacements]
      (let [m (cond
                (and (= 1 (count replacements))
                     (map? (first replacements)))
                (first replacements)

                (> (count replacements) 1)
                (apply hash-map replacements)

                :else
                (throw (new Exception "No replacements list passed to 'replace'"))) ]
        (reduce
          (fn [s [match replace-with]]
            (.replace s match replace-with))
          s
          m)))

    "rest"
    rest

    "reverse"
    (fn [x]
      (if (string? x)
        (string/reverse x)
        (reverse x)))

    "round"
    (fn [n]
      (Math/round n))

    "second"
    second

    "slice"
    (fn [x start length]
      (if (string? x)
        (subs x start (+ start length))
        (->> x (drop start) (take length))))

    "sort"
    (fn [x]
      (sort < x))

    "sort_descending"
    (fn [x]
      (sort > x))

    "sort_by"
    (fn [x k]
      (sort-by #(get % k) x))

    "sort_by_descending"
    (fn [x k]
      (sort-by #(get % k) #(compare %2 %1) x))

    "split"
    (fn [s delimiter]
      (string/split s (re-pattern delimiter)))

    "wrap"
    (fn [s max-width & [wrap-long-words? new-line-str]]
      (WordUtils/wrap s max-width new-line-str
                      (if (nil? wrap-long-words?)
                        false
                        wrap-long-words?)))

    }

   :tests
   {}})