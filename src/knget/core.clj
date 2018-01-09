
(ns knget.core
  (:gen-class)
  (:use [clojure.walk :as walk])
  (:require [pl.danieljanus.tagsoup :as ts]
	    [clojure.java.io :as io]
	    [clojure.tools.cli :refer [parse-opts]]
	    [me.raynes.conch :refer [programs with-programs let-programs] :as sh]))

(def cli-options
  [["-m" "--map MAP" "Title mapping"
    :default {}
    :parse-fn #(eval (read-string %))
    :validate [#(map? %) "Must be a valid clojure hashmap"]
    ]])

(defn mkbasename
  "Create a name based on the title, season number, and episode number
  extracted from the knowledge.net page. Note that the episode and/or
  season may be empty strings."
  [title season episode]
  (let [t (-> title
	      (clojure.string/replace #"['.,:;!?]" "")
	      (clojure.string/replace "&" "and")
	      (clojure.string/replace "/" "_")
	      (clojure.string/replace " " "_"))]
    (if (= (count episode) 0)
      t
      (let [e (Integer/parseInt episode)]
	(if (= (count season) 0)
	  (format "%s_ep%02d" t e)
	  (format "%s_s%02de%02d" t (Integer/parseInt season) e))))))

(defn mkvideoname [namemap title season episode]
  (let [name (format "%s.mp4" (mkbasename title season episode))]
    (if (nil? namemap)
      name
      (let [mapname (namemap name)]
	(if (nil? mapname)
	  name
	  mapname)))))

(defn mkvideourl [code]
  (str "https://content.jwplatform.com/feeds/" code ".json"))

(defn key-value?
  "Determines if specified vector represents a key-value."
  [v]
  (and (vector? v)
       (keyword? (first v))))

(defn videoid?
  "Has a non-empty videoid"
  [v]
  (and (contains? v :data-videoid)
       (pos? (count (v :data-videoid)))))

(defn video?
  "Determines if specified vector represents a video."
  [v n]
  (and (key-value? v)
       (= (count v) n) ; 4 for episode list, 2 for single show
       (= :a (first v))
       (videoid? (second v))))

(defn episode?
  "Determines if specified vector represents an episode."
  [v]
  (video? v 4))

(defn show?
  "Determines if specified vector represents a single show."
  [v]
  (video? v 2))

(defn findepisodes [m h]
  (let [episodes (atom {})
	collect-video
	(fn [f]
	  (fn [x] (if (f x)
	    (let [episode (second x)]
	      (swap! episodes assoc
		     (mkvideoname m
				  (episode :data-title)
				  (episode :x-tracking-season)
				  (episode :x-tracking-epnum))
		     (mkvideourl (episode :data-videoid))))
	    x)))]
    (walk/postwalk (collect-video episode?) h)
    (when (= (count @episodes) 0)
      (walk/postwalk (collect-video show?) h))
    @episodes))

(defn getformat
  "Determines the format to use when getting the video.
  Have found that the format codes that are between 1 and
  10 (inclusive) have the best results with youtube-dl."
  [url]
  (sh/programs youtube-dl)
  (let [supportedfmt (youtube-dl "-F" url {:seq true})
	findfmt (fn [s]
		  (let [f (re-find #"^\d+" s)]
		    (if (not (nil? f))
		      (Integer/parseInt f)
		      -1)))
	goodfmt (fn [n]
		  (and (> n 0) (<= n 10)))
	fmts (map findfmt supportedfmt)]
    (->> fmts
	 (filter goodfmt)
	 (apply max))))

(defn mkcmdname [videoname]
  (str videoname ".mak"))

(defn mkoutput [name url]
  (format "all::\t%s\n\n%s:\n\tyoutube-dl -f %d -o $@ \"%s\"\n"
	  name name (getformat url) url))

(defn mkmakefiles
  "Create a makefile for each episode in the specified map."
  [m]
  (doseq [k (keys m)]
    (let [name (mkcmdname k)]
      (when (not (.exists (io/file name)))
	(spit name (mkoutput k (m k)))))))

(defn -main [& args]
  (let [options (parse-opts args cli-options)
	urls (options :arguments)]
    (if (= (count urls) 0)
      (println "Must specify a knowledge.ca URL")
      (doseq [url urls]
	(->> url
	     (ts/parse)
	     (findepisodes ((options :options) :map))
	     (mkmakefiles))))))
