(ns pez.tasks
  (:require [babashka.fs :as fs]
            [babashka.http-client :as http]
            [clojure.java.io :as io]
            [pez.index :as index]))

(def awesome-dir "awesome-copilot-main")
(def zip-url "https://github.com/github/awesome-copilot/archive/refs/heads/main.zip")

(def cursorrules-dir "awesome-cursorrules-main")
(def cursorrules-zip-url "https://github.com/PatrickJS/awesome-cursorrules/archive/refs/heads/main.zip")

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

(defn ^:export download-cursorrules!
  "Get contents of the awesome-cursorrules repository without git history"
  [& _]
  (when (fs/exists? cursorrules-dir)
    (println "Removing existing" cursorrules-dir "directory...")
    (fs/delete-tree cursorrules-dir))
  (println "Downloading cursor rules repository ZIP archive...")
  (let [zip-file "awesome-cursorrules.zip"
        response (http/get cursorrules-zip-url {:as :stream})
        body     (:body response)]
    (with-open [in body
                out (io/output-stream zip-file)]
      (io/copy in out))
    (println "Extracting ZIP archive...")
    (fs/unzip zip-file ".")
    (fs/delete zip-file))
  (println "Cursor rules repository contents extracted to:" cursorrules-dir))

(defn ^:export generate-cursorrules-index!
  "Generate cursor rules index files from the downloaded awesome-cursorrules repository"
  [& _]
  (when-not (fs/exists? cursorrules-dir)
    (throw (ex-info (str "Cursor rules repository directory '" cursorrules-dir "' not found. Run 'bb download-cursorrules!' first.")
                    {:directory cursorrules-dir})))
  (index/generate-cursorrules-index! cursorrules-dir))

(defn ^:export generate-index!
  "Generate index files from the downloaded awesome-copilot repository"
  [& _]
  (when-not (fs/exists? awesome-dir)
    (throw (ex-info (str "Repository directory '" awesome-dir "' not found. Run 'bb download-awesome!' first.")
                    {:directory awesome-dir})))
  (index/generate-index! awesome-dir))

(def joyride-scripts
  "Scripts to copy from Joyride user config to site directory"
  ["awesome_copilot.cljs"
   "cursorrules_to_copilot.cljs"])

(defn ^:export copy-joyride-scripts!
  "Copy Joyride scripts from user config to site directory"
  [& _]
  (let [source-dir (fs/path (fs/home) ".config" "joyride" "scripts")
        target-dir "site"]
    (println "Copying Joyride scripts from" (str source-dir) "to" target-dir)
    (doseq [script joyride-scripts]
      (let [source (fs/path source-dir script)
            target (fs/path target-dir script)]
        (if (fs/exists? source)
          (do
            (println "  ✓" script)
            (fs/copy source target {:replace-existing true}))
          (println "  ✗" script "(not found at source)"))))
    (println "Joyride scripts copy complete!")))
