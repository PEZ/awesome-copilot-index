;; Script for Converting Cursor Rules to GitHub Copilot Instructions
;; Based on: https://github.com/PatrickJS/awesome-cursorrules
;;
;; Joyride: https://github.com/BetterThanTomorrow/joyride
;; Install it from the Extensions pane in VS Code
;;
;; Install in Joyride as User script:
;; 1. Select all script code + Copy
;; 2. In VS Code Command Palette: Joyride: Create User Script...
;;    * Name it 'awesome-cursorrules-to-copilot'
;; 3. In the editor that opens: Select all + Paste
;;
;; Use, from any VS Code window:
;; 1. Command Palette: Joyride: Run User Script...
;; 2. Select 'awesome_cursorrules_to_copilot.cljs'
;;
;; Hack the script, make it your own
;; 1. Command palette: Joyride: Open User Joyride Directory in New Window

(ns awesome-cursorrules-to-copilot
  (:require
   ["fs" :as fs]
   ["path" :as path]
   ["vscode" :as vscode]
   [clojure.string :as string]
   [joyride.core :as joyride]
   [promesa.core :as p]))

(def INDEX-URL "https://pez.github.io/awesome-copilot-index/awesome-cursorrules.json")
(def CONTENT-BASE-URL "https://raw.githubusercontent.com/PatrickJS/awesome-cursorrules/main/")

;; Preference management for picker memory
(def PREFS-KEY "cursor-rules-converter-preferences")

(defn get-vscode-user-dir []
  (let [context (joyride/extension-context)
        global-storage-uri (.-globalStorageUri context)
        global-storage-path (.-fsPath global-storage-uri)]
    ;; Get the User directory, which is two levels up from the extension's globalStorage directory
    ;; The path structure is: User/globalStorage/extension-id
    (-> global-storage-path
        path/dirname
        path/dirname)))

(defn get-preferences []
  (let [context (joyride/extension-context)
        global-state (.-globalState context)
        stored (.get global-state PREFS-KEY)]
    (if stored
      (js->clj (js/JSON.parse stored) :keywordize-keys true)
      {})))

(defn save-preference [key value]
  (let [context (joyride/extension-context)
        global-state (.-globalState context)
        current-prefs (get-preferences)
        updated-prefs (assoc current-prefs key value)]
    (.update global-state PREFS-KEY (js/JSON.stringify (clj->js updated-prefs)))))

(defn get-preference [key default-value]
  (get (get-preferences) key default-value))

(defn show-picker-with-memory+
  [items {:keys [title placeholder preference-key match-fn save-fn]}]
  (let [last-choice (get-preference preference-key nil)
        items-js (clj->js items)
        picker (vscode/window.createQuickPick)]

    (set! (.-items picker) items-js)
    (set! (.-title picker) title)
    (set! (.-placeholder picker) placeholder)
    (set! (.-ignoreFocusOut picker) true)

    (when last-choice
      (when-let [active-index (some->> items-js
                                       (map-indexed vector)
                                       (some (fn [[idx item]]
                                               (when (match-fn item last-choice) idx))))]
        (set! (.-activeItems picker) #js [(aget items-js active-index)])))

    ;; Return a promise that handles the user interaction
    (js/Promise.
     (fn [resolve _reject]
       (.onDidAccept picker
                     (fn []
                       (let [selected (first (.-selectedItems picker))]
                         (.hide picker)
                         (when selected
                           (let [selected-clj (js->clj selected :keywordize-keys true)]
                             (save-preference preference-key (save-fn selected-clj))
                             (resolve selected-clj))))))
       (.onDidHide picker
                   (fn []
                     (resolve nil)))
       (.show picker)))))

;; Copilot output formats
(def conversion-targets
  [{:label "Instructions"
    :iconPath (vscode/ThemeIcon. "list-ordered")
    :description "GitHub Copilot Instructions"
    :detail "Convert to .github/instructions/*.md format"
    :format "instructions"}
   {:label "Prompts"
    :iconPath (vscode/ThemeIcon. "chevron-right")
    :description "GitHub Copilot Prompts"
    :detail "Convert to .github/prompts/*.prompt.md format"
    :format "prompts"}
   {:label "Chat Modes"
    :iconPath (vscode/ThemeIcon. "color-mode")
    :description "GitHub Copilot Chat Modes"
    :detail "Convert to .github/chatmodes/*.chatmode.md format"
    :format "chatmodes"}
   {:label "View Content"
    :iconPath (vscode/ThemeIcon. "preview")
    :description "View cursor rule content"
    :detail "Preview the rule content in an editor"
    :format "view-content"}
   {:label "View README"
    :iconPath (vscode/ThemeIcon. "book")
    :description "View tech-stack README"
    :detail "Preview the README file for this tech-stack"
    :format "view-readme"}])

;; Actions - matching awesome_copilot exactly
(def actions
  [{:label "Install Globally"
    :iconPath (vscode/ThemeIcon. "globe")
    :description "Save to user profile"
    :detail "Available across all your workspaces"
    :action :global}
   {:label "Install in Workspace"
    :iconPath (vscode/ThemeIcon. "github-project")
    :description "Save to this workspace only"
    :detail "Only available in this project"
    :action :workspace}])

(defn fetch-index+ []
  (p/let [response (js/fetch INDEX-URL)
          data (.json response)
          clj-data (js->clj data :keywordize-keys true)]
    ;; Extract the actual rules from the cursor-rules key
    (get clj-data :cursor-rules)))

(defn fetch-component-content+ [link]
  (let [content-url (str CONTENT-BASE-URL link)]
    (p/let [response (js/fetch content-url)
            text (.text response)]
      text)))

(defn fetch-readme-content+ [component]
  (let [;; Extract the directory name from the component link
        link (:link component)
        ;; Get the directory part (e.g., "rules/nextjs-typescript-tailwind-cursorrules-prompt-file/")
        directory (-> link
                      (string/split #"/")
                      (->> (take 2))
                      (->> (string/join "/")))
        readme-url (str CONTENT-BASE-URL directory "/README.md")]
    (p/let [response (js/fetch readme-url)
            text (.text response)]
      text)))

(defn prepare-component-for-display [component]
  {:label (str (:tech-stack component) " - " (:domain component))
   :iconPath (vscode/ThemeIcon. "copilot")
   :description (str (:component-type component) " component")
   :detail (:description component)
   :component component})

(defn show-component-picker+ [components]
  (let [prepared-components (map prepare-component-for-display components)]
    (show-picker-with-memory+
     prepared-components
     {:title "Cursor Rules Converter"
      :placeholder "Select a cursor rule component to convert"
      :preference-key :last-component
      :match-fn (fn [item last-choice]
                  (= (-> item .-component .-link) (:link last-choice)))
      :save-fn (fn [selected-clj] (-> selected-clj :component))})))

(defn show-format-picker+ []
  (show-picker-with-memory+
   conversion-targets
   {:title "Cursor Rules Converter"
    :placeholder "Select output format"
    :preference-key :last-format
    :match-fn (fn [format-item last-choice] (= (.-format format-item) (name last-choice)))
    :save-fn :format}))

(defn show-action-picker+ [component _format]
  (show-picker-with-memory+
   actions
   {:title "Cursor Rules Converter"
    :placeholder (str "Action for " (:tech-stack component) " - " (:domain component))
    :preference-key :last-action
    :match-fn (fn [action-item last-choice] (= (name (.-action action-item)) (name last-choice)))
    :save-fn :action}))

(defn parse-frontmatter
  "Parse a cursorrules file into {:content ..., :frontmatter {...}}
   where frontmatter values are kept as raw strings (verbatim strategy)"
  [content]
  (if-let [frontmatter-match (re-find #"(?s)^---\s*\n(.*?)\n---\s*\n?(.*)" content)]
    (let [frontmatter-text (second frontmatter-match)
          body-text (string/trim (nth frontmatter-match 2))
          lines (string/split-lines frontmatter-text)
          ;; Parse frontmatter handling multi-line values
          frontmatter-map (loop [lines lines
                                 acc {}
                                 current-key nil
                                 current-value ""]
                            (if (empty? lines)
                              ;; Final key-value pair
                              (if current-key
                                (assoc acc current-key (string/trim current-value))
                                acc)
                              (let [line (first lines)
                                    remaining (rest lines)]
                                (if-let [key-match (re-find #"^([^:]+):\s*(.*)$" line)]
                                  ;; New key found
                                  (let [new-key (keyword (string/trim (second key-match)))
                                        new-value (string/trim (nth key-match 2))
                                        ;; Save previous key-value if exists
                                        updated-acc (if current-key
                                                      (assoc acc current-key (string/trim current-value))
                                                      acc)]
                                    (recur remaining updated-acc new-key new-value))
                                  ;; Continuation line (indented or blank)
                                  (if current-key
                                    (recur remaining acc current-key
                                           (str current-value "\n" line))
                                    (recur remaining acc current-key current-value))))))]
      {:content body-text
       :frontmatter frontmatter-map})
    ;; No frontmatter found
    {:content content
     :frontmatter {}}))

(defn convert-to-instructions
  "Convert cursor rule to GitHub Copilot Instructions format"
  [component parsed]
  (let [tech-stack (:tech-stack component)
        domain (:domain component)
        description (:description component)
        rule-content (:content parsed)
        frontmatter (:frontmatter parsed)
        apply-to (:globs frontmatter)]

    (str "---\n"
         "description: " description "\n"
         (when apply-to
           (str "applyTo: " apply-to "\n"))
         "---\n\n"
         "# " tech-stack " - " domain "\n\n"
         rule-content)))

(defn convert-to-prompt
  "Convert cursor rule to GitHub Copilot Prompt format"
  [component _content parsed]
  (let [tech-stack (:tech-stack component)
        domain (:domain component)
        description (:description component)
        rule-content (:content parsed)]

    (str "---\n"
         "description: " description "\n"
         "---\n\n"
         "# " tech-stack " - " domain "\n\n"
         rule-content)))

(defn convert-to-chatmode
  "Convert cursor rule to GitHub Copilot Chat Mode format"
  [component _content parsed]
  (let [tech-stack (:tech-stack component)
        domain (:domain component)
        description (:description component)
        rule-content (:content parsed)]

    (str "---\n"
         "description: " description "\n"
         "---\n\n"
         "# " tech-stack " - " domain "\n\n"
         "This chat mode applies " tech-stack " best practices for " domain ".\n\n"
         rule-content)))

(defn convert-content
  "Convert cursor rule content to the specified format"
  [component content format]
  (let [parsed (parse-frontmatter content)]
    (case format
      "instructions" (convert-to-instructions component parsed)
      "prompts" (convert-to-prompt component content parsed)
      "chatmodes" (convert-to-chatmode component content parsed)
      content))) ; fallback to original content

(defn get-filename
  "Generate appropriate filename for the converted content"
  [component format]
  (let [tech-stack (-> (:tech-stack component)
                       (string/lower-case)
                       (string/replace #"[^a-z0-9]" "-")
                       (string/replace #"^-|-$" ""))
        domain (-> (:domain component)
                   (string/lower-case)
                   (string/replace #"[^a-z0-9]+" "-")
                   (string/replace #"^-|-$" ""))]
    (case format
      "instructions" (str tech-stack "-" domain ".md")
      "prompts" (str tech-stack "-" domain ".prompt.md")
      "chatmodes" (str tech-stack "-" domain ".chatmode.md")
      (str tech-stack "-" domain ".md"))))

(defn install-globally!
  "Install converted content globally to user profile"
  [content component format]
  (let [vscode-user-dir (get-vscode-user-dir)
        dir-path (cond
                   ;; Instructions go in .vscode/instructions in user home
                   (= format "instructions")
                   (path/join js/process.env.HOME ".vscode" "instructions")

                   ;; Both prompts and chatmodes go in User/prompts folder
                   (or (= format "prompts") (= format "chatmodes"))
                   (path/join vscode-user-dir "prompts")

                   ;; Unknown format
                   :else nil)

        filename (get-filename component format)]

    (if dir-path
      (try
        (when-not (fs/existsSync dir-path)
          (fs/mkdirSync dir-path #js {:recursive true}))

        (let [file-path (path/join dir-path filename)]
          (fs/writeFileSync file-path content)
          (vscode/window.showInformationMessage
           (str "Installed " filename " to " vscode/env.appName " User directory"))

          {:success true :path file-path})
        (catch :default err
          (vscode/window.showErrorMessage
           (str "Failed to install " filename ": " (.-message err)))
          {:success false :error (.-message err)}))

      (do
        (vscode/window.showErrorMessage
         (str "Unknown format: " format))
        {:success false :error (str "Unknown format: " format)}))))

(defn install-to-workspace!
  "Install converted content to workspace"
  [converted-content component format]
  (if-let [workspace-folder (first vscode/workspace.workspaceFolders)]
    (let [filename (get-filename component format)
          workspace-path (-> workspace-folder .-uri .-fsPath)
          dir-path (case format
                     "instructions" (path/join workspace-path ".github" "instructions")
                     "prompts" (path/join workspace-path ".github" "prompts")
                     "chatmodes" (path/join workspace-path ".github" "chatmodes")
                     nil)]

      (if dir-path
        (do
          (when-not (fs/existsSync dir-path)
            (fs/mkdirSync dir-path #js {:recursive true}))

          (let [file-path (path/join dir-path filename)]
            (fs/writeFileSync file-path converted-content)
            (vscode/window.showInformationMessage
             (str "Installed " filename " to workspace"))

            {:success true :path file-path}))

        (do
          (vscode/window.showErrorMessage
           (str "Unknown format: " format))
          {:success false :error "Unknown format"})))

    (do
      (vscode/window.showErrorMessage "No workspace folder open")
      {:success false :error "No workspace folder"})))

(defn open-file+
  "Open the created file in VS Code"
  [file-path]
  (p/let [uri (vscode/Uri.file file-path)
          doc (vscode/workspace.openTextDocument uri)
          _ (vscode/window.showTextDocument doc)]
    {:success true}))

(defn open-in-untitled-editor+
  "Show a preview of the converted content"
  [content]
  (p/let [doc (vscode/workspace.openTextDocument #js {:content content
                                                      :language "markdown"})
          _ (vscode/window.showTextDocument doc)]
    {:success true}))

(defn main []
  (p/catch
   (p/let [components (fetch-index+)
           selected-component (show-component-picker+ components)]
     (when selected-component
       (p/let [format-choice (show-format-picker+)]
         (when format-choice
           (cond
             (= (:format format-choice) "view-readme")
             ;; If user chose to view README, fetch and display it directly
             (p/let [readme-content (fetch-readme-content+ (:component selected-component))]
               (open-in-untitled-editor+ readme-content))

             (= (:format format-choice) "view-content")
             ;; If user chose to view content, fetch and display it directly
             (p/let [content (fetch-component-content+ (-> selected-component :component :link))]
               (open-in-untitled-editor+ content))

             :else
             ;; Otherwise, proceed with the normal conversion flow
             (p/let [content (fetch-component-content+ (-> selected-component :component :link))
                     converted-content (convert-content (:component selected-component)
                                                        content
                                                        (:format format-choice))

                     ;; Show action menu
                     action (show-action-picker+ (:component selected-component) (:format format-choice))]

               (when action
                 (case (keyword (:action action))
                   :global (p/let [result (install-globally! converted-content
                                                             (:component selected-component)
                                                             (:format format-choice))]
                             (when (:success result)
                               (open-file+ (:path result))))

                   :workspace (p/let [result (install-to-workspace! converted-content
                                                                    (:component selected-component)
                                                                    (:format format-choice))]
                                (when (:success result)
                                  (open-file+ (:path result))))))))))))

   (fn [error]
     (vscode/window.showErrorMessage (str "Error in cursor-rules-converter: " (.-message error)))
     (js/console.error "Error in cursor-rules-converter:" error))))

;; Run the script directly when loaded, unless loaded in the REPL
(when (= (joyride/invoked-script) joyride/*file*)
  (main))
