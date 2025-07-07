;; Script for Exploring and installing instructions and prompts from Awesome Copilot
;; https://github.com/github/awesome-copilot
;;
;; Joyride: https://github.com/BetterThanTomorrow/joyride
;; Install it from the Extensions pane in VS Code
;;
;; Install in Joyride as User script:
;; 1. Select all script code + Copy
;; 2. In VS Code Command Palette: Joyride: Create User Script...
;;    * Name it 'awesone-copilot'
;; 3. In the editor that opens: Select all + Paste
;;
;; Use, from any VS Code window:
;; 1. Command Palette: Joyride: Run User Script...
;; 2. Select 'awesome_copilot.cljs'
;;
;; Hack the script, make it your own
;; 1. Command palette: Joyride: Open User Joyride Directory in New Window

(ns awesome-copilot
  (:require ["vscode" :as vscode]
            ["path" :as path]
            ["fs" :as fs]
            [promesa.core :as p]
            [joyride.core :as joyride]))

(def INDEX-URL "https://pez.github.io/awesome-copilot-index/awesome-copilot.json")
(def CONTENT-BASE-URL "https://raw.githubusercontent.com/github/awesome-copilot/main/")
;; Preference management for picker memory
(def PREFS-KEY "awesome-copilot-preferences")

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

(def categories
  [{:label "Instructions"
    :iconPath (vscode/ThemeIcon. "list-ordered")
    :description "Coding styles and best practices"
    :detail "Guidelines for generating code that follows specific patterns"
    :category "instructions"}
   {:label "Prompts"
    :iconPath (vscode/ThemeIcon. "chevron-right")
    :description "Task-specific templates"
    :detail "Pre-defined prompts for common tasks like testing, documentation, etc."
    :category "prompts"}
   {:label "Chatmodes"
    :iconPath (vscode/ThemeIcon. "color-mode")
    :description "Conversation behavior settings"
    :detail "Configure how Copilot Chat behaves for different activities"
    :category "chatmodes"}])

(def actions
  [{:label "View Content"
    :iconPath (vscode/ThemeIcon. "preview")
    :description "Open in untitled editor"
    :detail "Preview the markdown content in an editor"
    :action :view}
   {:label "Install Globally"
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
    clj-data))

(defn fetch-content+ [link]
  (let [content-url (str CONTENT-BASE-URL link)]
    (p/let [response (js/fetch content-url)
            text (.text response)]
      text)))

(defn show-category-picker+ []
  (show-picker-with-memory+
   categories
   {:title "Awesome Copilot"
    :placeholder "Select Awesome Copilot category"
    :preference-key :last-category
    :match-fn (fn [item last-choice] (= (.-category item) last-choice))
    :save-fn :category}))

(defn show-item-picker [items category-name]
  (let [items-with-metadata (map (fn [item]
                                   {:label (:title item)
                                    :iconPath (vscode/ThemeIcon. "copilot")
                                    :description (:filename item)
                                    :detail (:description item)
                                    :item item})
                                 items)
        preference-key (keyword (str "last-item-" category-name))]

    (show-picker-with-memory+
     items-with-metadata
     {:title "Awesome Copilot"
      :placeholder (str "Select a " category-name " item")
      :preference-key preference-key
      :match-fn (fn [item last-choice]
                  (= (-> item .-item .-filename) (:filename last-choice)))
      :save-fn (fn [selected-clj] (-> selected-clj :item))})))

(defn show-action-menu+ [item]
  (show-picker-with-memory+
   actions
   {:title "Awesome Copilot"
    :placeholder (str "Action for " (-> item :item :title))
    :preference-key :last-action
    :match-fn (fn [action-item last-choice] (= (name (.-action action-item)) (name last-choice)))
    :save-fn :action}))

(defn open-in-untitled-editor+ [content _]
  (p/let [doc (vscode/workspace.openTextDocument #js {:content content
                                                      :language "markdown"})
          _ (vscode/window.showTextDocument doc)]
    {:success true}))

(defn install-globally! [content item category]
  (p/let [vscode-user-dir (get-vscode-user-dir)
          dir-path (cond
                     ;; Instructions go in .vscode/instructions in user home
                     (= category "instructions")
                     (path/join js/process.env.HOME ".vscode" "instructions")

                     ;; Both prompts and chatmodes go in User/prompts folder
                     (or (= category "prompts") (= category "chatmodes"))
                     (path/join vscode-user-dir "prompts")

                     ;; Unknown category
                     :else nil)

          filename (-> item :item :filename)]

    (if dir-path
      (try
        (when-not (fs/existsSync dir-path)
          (fs/mkdirSync dir-path #js {:recursive true}))

        (let [file-path (path/join dir-path filename)]
          (fs/writeFileSync file-path content)
          (vscode/window.showInformationMessage
           (str "Installed " filename " to " (.-appName vscode/env) " User/prompts directory"))

          {:success true :path file-path})
        (catch :default err
          (vscode/window.showErrorMessage
           (str "Failed to install " filename ": " (.-message err)))
          {:success false :error (.-message err)}))

      (do
        (vscode/window.showErrorMessage
         (str "Unknown category: " category))
        {:success false :error (str "Unknown category: " category)}))))

(defn install-to-workspace! [content item category]
  (if-let [workspace-folder (first vscode/workspace.workspaceFolders)]
    (let [filename (:filename (:item item))
          workspace-path (-> workspace-folder .-uri .-fsPath)
          dir-path (case category
                     "instructions" (path/join workspace-path ".github" "instructions")
                     "prompts" (path/join workspace-path ".github" "prompts")
                     "chatmodes" (path/join workspace-path ".github" "chatmodes")
                     nil)]

      (if dir-path
        (do
          (when-not (fs/existsSync dir-path)
            (fs/mkdirSync dir-path #js {:recursive true}))

          (let [file-path (path/join dir-path filename)]
            (fs/writeFileSync file-path content)
            (vscode/window.showInformationMessage
             (str "Installed " filename " to workspace"))

            {:success true :path file-path}))

        (do
          (vscode/window.showErrorMessage
           (str "Unknown category: " category))
          {:success false :error "Unknown category"})))

    (do
      (vscode/window.showErrorMessage "No workspace folder open")
      {:success false :error "No workspace folder"})))

;; Special handling for instructions to copilot-instructions.md
(defn install-to-copilot-instructions! [content item]
  (if-let [workspace-folder (first vscode/workspace.workspaceFolders)]
    (let [workspace-path (-> workspace-folder .-uri .-fsPath)
          github-dir (path/join workspace-path ".github")
          file-path (path/join github-dir "copilot-instructions.md")]

      ;; Create .github directory if it doesn't exist
      (when-not (fs/existsSync github-dir)
        (fs/mkdirSync github-dir #js {:recursive true}))

      ;; Check if file already exists for append vs create
      (if (fs/existsSync file-path)
        ;; Append mode
        (p/let [choice (vscode/window.showQuickPick
                        (clj->js [{:label "Append"
                                   :iconPath (vscode/ThemeIcon. "add")
                                   :description "Add to existing instructions"}
                                  {:label "Replace"
                                   :iconPath (vscode/ThemeIcon. "replace-all")
                                   :description "Overwrite existing instructions"}])
                        #js {:placeHolder "How to install to copilot-instructions.md?"})
                choice-clj (when choice (js->clj choice :keywordize-keys true))
                choice-text (when choice-clj (:label choice-clj))]
          (cond
            (= choice-text "Append")
            (let [existing-content (fs/readFileSync file-path #js {:encoding "utf-8"})
                  new-content (str existing-content "\n\n" content)]
              (fs/writeFileSync file-path new-content)
              (vscode/window.showInformationMessage
               (str "Appended " (-> item :item :filename) " to copilot-instructions.md"))
              {:success true :path file-path :mode "append"})

            (= choice-text "Replace")
            (do
              (fs/writeFileSync file-path content)
              (vscode/window.showInformationMessage
               "Replaced copilot-instructions.md")
              {:success true :path file-path :mode "replace"})

            :else
            {:success false :error "Cancelled or no choice made"}))

        ;; Create new file
        (do
          (fs/writeFileSync file-path content)
          (vscode/window.showInformationMessage
           "Created copilot-instructions.md")
          {:success true :path file-path :mode "create"})))

    ;; Error - no workspace folder
    (do
      (vscode/window.showErrorMessage "No workspace folder open")
      {:success false :error "No workspace folder"})))

(defn open-installed-file+ [file-path]
  (p/let [uri (vscode/Uri.file file-path)
          doc (vscode/workspace.openTextDocument uri)
          _ (vscode/window.showTextDocument doc)]
    {:success true}))

(defn execute-action! [item action-type category]
  (p/let [content (fetch-content+ (-> item :item :link))]
    (case (keyword action-type)
      :view
      (open-in-untitled-editor+ content (-> item :item :filename))

      :global
      (p/let [result (install-globally! content item category)]
        (when (:success result)
          (open-installed-file+ (:path result)))
        result)

      :workspace
      (if (= category "instructions")
        (p/let [choice (vscode/window.showQuickPick
                        (clj->js [{:label "GitHub Instructions Directory"
                                   :iconPath (vscode/ThemeIcon. "file-directory")
                                   :description ".github/instructions/"}
                                  {:label "Copilot Instructions File"
                                   :iconPath (vscode/ThemeIcon. "file-code")
                                   :description ".github/copilot-instructions.md"}])
                        #js {:placeHolder "Where to install?"})
                choice-clj (when choice (js->clj choice :keywordize-keys true))
                choice-text (when choice-clj (:label choice-clj))]
          (if (= choice-text "Copilot Instructions File")
            (p/let [result (install-to-copilot-instructions! content item)]
              (when (:success result)
                (open-installed-file+ (:path result)))
              result)
            (p/let [result (install-to-workspace! content item category)]
              (when (:success result)
                (open-installed-file+ (:path result)))
              result)))
        (p/let [result (install-to-workspace! content item category)]
          (when (:success result)
            (open-installed-file+ (:path result)))
          result))

      ;; Unknown action
      (do
        (vscode/window.showErrorMessage (str "Unknown action: " action-type))
        {:success false :error "Unknown action"}))))

(defn main []
  (p/catch
   (p/let [index (fetch-index+)
           category (show-category-picker+)]
     (when category
       (p/let [category-name (:category category)
               category-items (get index (keyword category-name))
               item (show-item-picker category-items (subs category-name 0 (dec (count category-name))))]
         (when item
           (p/let [action (show-action-menu+ item)]
             (when action
               (execute-action! item (:action action) category-name)))))))

   (fn [error]
     (vscode/window.showErrorMessage (str "Error: " (.-message error)))
     (js/console.error "Error in awesome-copilot:" error))))

;; Run the script directly when loaded, unless loaded in the REPL
(when (= (joyride/invoked-script) joyride/*file*)
  (main))