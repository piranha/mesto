(defproject solovyov/mesto "0.3.2"
  :description "In-memory storage for ClojureScript applications"
  :url "http://github.com/piranha/mesto/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :plugins [[lein-cljsbuild "0.2.9" :hooks false]]
  :cljsbuild {:builds
              {
               :main {
                      :source-path "src"
                      :compiler {:output-to "build/mesto.js"}
                      }
               :mini {
                      :source-path "src"
                      :compiler {
                                 :output-to "build/mini.js"
                                 :optimizations :advanced
                                 }
                      }
               }
              }
  )
