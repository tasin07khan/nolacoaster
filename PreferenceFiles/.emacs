(cond ((fboundp 'global-font-lock-mode)
       ;; Turn on font-lock in all modes that support it
       (global-font-lock-mode t)
       ;; Maximum colors
       (setq font-lock-maximum-decoration t)))

;;(setq auto-mode-alist (cons '("\\.ml\\w?" . tuareg-mode) auto-mode-alist))
;;(autoload 'tuareg-mode "tuareg" "Major mode for editing Caml code" t)
;;(autoload 'camldebug "camldebug" "Run the Caml debugger" t)

;; Turn on Fortress mode
(load (concat (getenv "FORTRESS_HOME")
	      "/Emacs/fortress-mode.el"))
(push '("\\.fs[si]$" . fortress-mode) auto-mode-alist)

;; Turn on Fortify
(load (concat (getenv "FORTRESS_HOME")
	      "/Fortify/fortify.el"))

;; Turn on SML mode
(add-to-list 'load-path "I:\\")
(autoload 'sml-mode "sml-mode" "Major mode for editing Standard ML" t)
(autoload 'htmlize "htmlize" "This will HTMLize your shit." t)

(setq-default ispell-program-name "aspell")

;;Advice, probably stolen from the emacs extensions book.
(defadvice switch-to-buffer (before existing-buffer  			
				    activate compile)  	
  "When interactive, switch to existing buffers only, 
unless given a prefix argument."  	
  (interactive  			
   (list (read-buffer "Switch to buffer:"
		      (other-buffer)  	
		      (null current-prefix-arg))))) 



;;A bunch of my own AWESOME functions
(defun other-window-backward (n)
  "Select the previous window."
  (interactive "p")
  (other-window (- n)))

;;AWESOME hooks
(add-hook 'font-lock-mode-hook
	  '(lambda ()
	     (font-lock-add-keywords
	      nil
	      '(("\\(TODO\\)" 1
		 font-lock-warning-face t)))
	     (set-face-foreground 'font-lock-warning-face
				  "red")))

;;A bunch of my own AWESOME keyboard bindings
(global-set-key "\M-?" 'help-command)
(global-set-key "\C-h" 'delete-backward-char)
(global-set-key "\C-x\C-n" 'other-window)
(global-set-key "\C-x\C-p" 'other-window-backward)
(global-set-key "\M-g" 'goto-line)

;;Other variables

;;Things I need to do to set tabs to be awesome in Java
;;java-mode-hook
;;set c-basic-offset 2
;;set tab-width 2
;;load save hook so that on save tabification is performed.
(defun tabify-buffer (&optional noisy)
  "This tabifies the entire buffer, leaving the cursor where it is."
  (interactive "P")
  (save-excursion
    (tabify (point-min) (point-max))
    (if noisy () (message "[NEB] Tabified Buffer"))))

(defun tabify-before-save ()
  (progn
    (tabify-buffer 0)
    nil))

(defun save-with-tabs ()
  (progn
    (add-hook 'local-write-file-hooks 'tabify-before-save)
    nil))

(defun my-java-hook ()
  (progn
    (save-with-tabs)
    (setq tab-width 2)
    (setq c-basic-offset 2)
    nil))

(add-hook 'java-mode-hook 'my-java-hook)
	  




(setq auto-mode-alist
      (append '(("\\.sml$" . sml-mode)
		("\\.sig$" . sml-mode)
		("\\.ML$"  . sml-mode)) auto-mode-alist))

(defun frame-settings (f)
  (progn
    (if (null f) () (select-frame f))
    (menu-bar-mode -1)
    (tool-bar-mode -1)
    (blink-cursor-mode -1)
    (set-background-color "grey15")
    (set-foreground-color "grey80")
;; Do this if you are using Linux. 
;; Look how crazy their freaking font names are...
;;    (set-default-font
;;     "-misc-fixed-bold-r-normal--13-100-100-100-c-70-iso8859-1")
    (set-default-font "FixedSys")))

(add-hook 'after-make-frame-functions 'frame-settings)

(frame-settings nil)

(global-set-key "\C-h" 'backward-delete-char)

;;(global-set-key "\C-SPC" 'set-mark-command)
(custom-set-variables
  ;; custom-set-variables was added by Custom -- don't edit or cut/paste it!
  ;; Your init file should contain only one such instance.
 )
(custom-set-faces
  ;; custom-set-faces was added by Custom -- don't edit or cut/paste it!
  ;; Your init file should contain only one such instance.
 '(font-lock-comment-face ((((class color) (background dark)) (:foreground "#11AABB"))))
 '(font-lock-doc-face ((t (:foreground "#CCBB99"))))
 '(font-lock-string-face ((((class color) (background dark)) (:foreground "#119911")))))
