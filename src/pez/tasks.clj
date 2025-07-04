(ns pez.tasks
  (:require [babashka.tasks :refer [shell]]
            [babashka.fs :as fs]))

(defn ^:export clone-awesome
  "Get contents of the awesome-copilot repository without git history"
  [& _]
  (let [repo-dir "awesome-copilot"]
    ;; Remove existing directory if it exists
    (when (fs/exists? repo-dir)
      (println "Removing existing" repo-dir "directory...")
      (fs/delete-tree repo-dir))

    ;; Do a shallow clone
    (println "Cloning repository...")
    (shell {:dir "."} "git clone --depth 1 https://github.com/github/awesome-copilot.git")

    ;; Then remove the .git directory
    (fs/delete-tree (fs/path repo-dir ".git"))
    (println "Repository contents extracted to:" repo-dir)))
