(ns pez.tasks
  (:require [babashka.fs :as fs]
            [babashka.http-client :as http]
            [clojure.java.io :as io]))

(def awesome-dir "awesome-copilot-main")
(def zip-url "https://github.com/github/awesome-copilot/archive/refs/heads/main.zip")

(defn ^:export download-awesome!
  "Get contents of the awesome-copilot repository without git history"
  [& _]
  (when (fs/exists? awesome-dir)
    (println "Removing existing" awesome-dir "directory...")
    (fs/delete-tree awesome-dir))
  (println "Downloading repository ZIP archive...")
  (let [zip-file "awesome-copilot.zip"
        response (http/get zip-url {:as :stream})
        body     (:body response)]
    (with-open [in body
                out (io/output-stream zip-file)]
      (io/copy in out))
    (println "Extracting ZIP archive...")
    (fs/unzip zip-file ".")
    (fs/delete zip-file))
  (println "Repository contents extracted to:" awesome-dir))
