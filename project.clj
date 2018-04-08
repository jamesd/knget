(defproject knget "0.2.2"
  :description "Get video codes from knowledge.ca pages"
  :url "https://github.com/jamesd/knget"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
		 [clj-tagsoup "0.3.0" :exclusions [org.clojure/clojure]]
		 [me.raynes/conch "0.8.0"]
		 [org.clojure/tools.cli "0.3.5"]]
  :main ^:skip-aot knget.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
