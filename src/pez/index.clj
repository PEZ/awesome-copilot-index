(ns pez.index
  (:require
   [babashka.fs :as fs]
   [cheshire.core :as json]
   [clojure.pprint :as pprint]
   [clojure.string :as str]
   [selmer.parser :as selmer]))

(defn parse-markdown-file
  "Parse a markdown file with frontmatter into frontmatter and content sections"
  [content]
  (let [lines (str/split-lines content)]
    (if (= "---" (first lines))
      (let [end-idx (->> lines
                        rest
                        (map-indexed vector)
                        (filter #(= "---" (second %)))
                        first
                        first)
            frontmatter-lines (take end-idx (rest lines))
            content-lines (drop (+ end-idx 2) lines)]
        {:frontmatter frontmatter-lines
         :content content-lines})
      {:frontmatter []
       :content lines})))

(defn parse-frontmatter
  "Parse YAML-style frontmatter lines into a map"
  [frontmatter-lines]
  (->> frontmatter-lines
       (map #(str/split % #":\s*" 2))
       (filter #(= 2 (count %)))
       (map (fn [[k v]] [(keyword k) (str/replace v #"^'|'$" "")]))
       (into {})))

(defn filename-to-title
  "Convert a filename to a human-readable title"
  [filename]
  (-> filename
      (str/replace #"\.(instructions|prompt|chatmode)\.md$" "")
      (str/replace #"[-_]" " ")
      (str/split #" ")
      (->> (map str/capitalize)
           (str/join " "))))

(defn extract-title
  "Extract title from frontmatter first, then H1 heading, finally fallback to filename-based title"
  [content-lines frontmatter filename]
  (or (:title frontmatter)
      (->> content-lines
           (filter #(str/starts-with? % "# "))
           first
           (#(when % (str/replace % #"^# " ""))))
      (filename-to-title filename)))

(defn process-file
  "Process a single markdown file and extract metadata"
  [file-path base-dir]
  (let [file-str (str file-path)
        content (slurp file-str)
        parsed (parse-markdown-file content)
        frontmatter (parse-frontmatter (:frontmatter parsed))
        filename (fs/file-name file-path)
        title (extract-title (:content parsed) frontmatter filename)
        relative-path (str/replace file-str (str (fs/path base-dir) fs/file-separator) "")]
    {:filename filename
     :title title
     :description (:description frontmatter)
     :link relative-path}))

(defn get-markdown-files
  "Get all markdown files in a directory"
  [dir]
  (->> (fs/list-dir dir)
       (filter #(str/ends-with? (str %) ".md"))
       (sort)))

(defn process-directory
  "Process all markdown files in a directory"
  [dir base-dir]
  (->> (get-markdown-files dir)
       (map #(process-file % base-dir))))

(defn format-stats-table
  "Format statistics for template rendering"
  [index-data]
  (let [{:keys [instructions prompts chatmodes generated]} index-data
        instruction-count (count instructions)
        prompt-count (count prompts)
        chatmode-count (count chatmodes)
        total-count (+ instruction-count prompt-count chatmode-count)]
    {:total-count total-count
     :instruction-count instruction-count
     :prompt-count prompt-count
     :chatmode-count chatmode-count
     :generated generated
     :formatted-date (.format (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss 'UTC'")
                              (java.time.ZonedDateTime/parse generated))}))

(defn render-index-page!
  "Render the index page from template with data"
  [index-data]
  (let [template (slurp "site/index-template.md")
        stats (format-stats-table index-data)
        rendered (selmer/render template stats)]
    (println "Writing site/index.md...")
    (spit "site/index.md" rendered)))

(defn generate-index!
  "Generate index files from awesome-copilot repository contents"
  ([] (generate-index! "awesome-copilot-main"))
  ([base-dir]
   (println "Generating index from" base-dir "...")
   (let [instructions (process-directory (fs/path base-dir "instructions") base-dir)
         prompts (process-directory (fs/path base-dir "prompts") base-dir)
         chatmodes (process-directory (fs/path base-dir "chatmodes") base-dir)
         index-data {:generated (str (java.time.Instant/now))
                     :instructions instructions
                     :prompts prompts
                     :chatmodes chatmodes}]
     (println "Writing awesome-copilot.json...")
     (spit "site/awesome-copilot.json" (json/generate-string index-data {:pretty true}))
     (println "Writing awesome-copilot.edn...")
     (spit "site/awesome-copilot.edn" (with-out-str (pprint/pprint index-data)))
     (render-index-page! index-data)
     (println "Index generation complete!")
     index-data)))

;; Cursor rules processing functions

(defn directory-name-to-tech-stack
  "Convert directory name to human-readable tech stack"
  [dir-name]
  (-> dir-name
      (str/replace #"-cursorrules-prompt-file$" "")
      (str/replace #"-" " ")
      (str/split #" ")
      (->> (map (fn [word]
                  (case word
                    "nextjs" "Next.js"
                    "fastapi" "FastAPI"
                    "js" "JS/JavaScript"
                    "ts" "TS/TypeScript"
                    "typescript" "TS/TypeScript"
                    "node" "Node.js"
                    "nodejs" "Node.js"
                    "php" "PHP"
                    "csharp" "C#/csharp"
                    "dotnet" ".NET/dotnet"
                    "net" ".NET/dotnet"
                    "cpp" "C++/cpp"
                    "c++" "C++/cpp"
                    (str/capitalize word))))
           (str/join " "))))

(defn filename-to-domain
  "Convert filename to human-readable domain"
  [filename]
  (-> filename
      (str/replace #"\.(mdc|mdx)$" "")
      (str/replace #"[-_]" " ")
      (str/split #" ")
      (->> (map str/capitalize)
           (str/join " "))))

(defn get-component-type
  "Determine component type from filename"
  [filename]
  (cond
    (str/ends-with? filename ".mdc") "mdc"
    (str/ends-with? filename ".mdx") "mdx"
    :else nil))

(defn get-cursor-rule-files
  "Get all .mdc and .mdx files in a directory (exclude .cursorrules and README.md)"
  [dir]
  (->> (fs/list-dir dir)
       (filter #(let [filename (fs/file-name %)]
                  (and (or (str/ends-with? filename ".mdc")
                           (str/ends-with? filename ".mdx"))
                       (not= filename "README.md"))))
       (sort)))

(defn process-cursor-rule-file
  "Process a single cursor rule file and extract metadata"
  [file-path base-dir tech-stack]
  (let [file-str (str file-path)
        content (slurp file-str)
        parsed (parse-markdown-file content)
        frontmatter (parse-frontmatter (:frontmatter parsed))
        filename (fs/file-name file-path)
        domain (filename-to-domain filename)
        relative-path (str/replace file-str (str (fs/path base-dir) fs/file-separator) "")
        component-type (get-component-type filename)]
    {:description (:description frontmatter)
     :tech-stack tech-stack
     :domain domain
     :link relative-path
     :component-type component-type}))

(defn process-cursorrules-tech-directory
  "Process a single technology directory for cursor rules"
  [tech-dir base-dir]
  (let [dir-name (fs/file-name tech-dir)
        tech-stack (directory-name-to-tech-stack dir-name)
        cursor-rule-files (get-cursor-rule-files tech-dir)]
    (when (seq cursor-rule-files)
      (println "Processing" dir-name "(" tech-stack ") -" (count cursor-rule-files) "files")
      (->> cursor-rule-files
           (map #(process-cursor-rule-file % base-dir tech-stack))))))

(defn process-cursorrules-directory
  "Process all technology directories in the cursor rules repository"
  [base-dir]
  (println "Processing cursor rules from" base-dir "...")
  (let [rules-dir (fs/path base-dir "rules")]
    (->> (fs/list-dir rules-dir)
         (filter #(and (fs/directory? %)
                       (str/ends-with? (fs/file-name %) "-cursorrules-prompt-file")))
         (mapcat #(process-cursorrules-tech-directory % base-dir))
         (filter some?)
         (vec))))

(defn generate-cursorrules-index!
  "Generate cursor rules index files from repository contents"
  ([] (generate-cursorrules-index! "awesome-cursorrules-main"))
  ([base-dir]
   (println "Generating cursor rules index from" base-dir "...")
   (let [cursor-rules (process-cursorrules-directory base-dir)
         index-data {:generated (str (java.time.Instant/now))
                     :cursor-rules cursor-rules}]
     (println "Found" (count cursor-rules) "cursor rule components")
     (println "Writing site/cursor-rules.json...")
     (spit "site/cursor-rules.json" (json/generate-string index-data {:pretty true}))
     (println "Writing site/cursor-rules.edn...")
     (spit "site/cursor-rules.edn" (with-out-str (pprint/pprint index-data)))
     (println "Cursor rules index generation complete!")
     index-data)))

(comment
  (generate-index!)
  (generate-cursorrules-index!)
  :rcf)

