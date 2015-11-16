;;; $ProjectHeader: use 2-1-0-release.1 Sun, 09 May 2004 13:57:11 +0200 mr $
;;; generic emacs mode for USE specifications

(require 'generic)

(define-generic-mode 'use-generic-mode
  (list "--")
  (list 
   "abstract"
   "and"
   "association"
   "aggregation"
   "attributes"
   "between"
   "class"
   "composition"
   "constraints"
   "context"
   "else"
   "end"
   "endif"
   "enum"
   "if"
   "implies"
   "inv"
   "let"
   "model"
   "not"
   "operations"
   "or"
   "ordered"
   "pre"
   "post"
   "role"
   "then"
   "xor"
   )
  (list
   ;; class and association names
   (list "\\(class\\|enum\\|association\\|composition\\|aggregation\\|context\\|model\\)[ \t]*\\([^ \t\n]+\\)[ \t\n]" 
	 '(2 font-lock-type-face))
   ;; operations
   (list "\\(\\w+(\.*)\\s-*:\\s-*\\(\\w\\|(\\|)\\)+\\)" 
	 '(1 font-lock-function-name-face))
   ;; multiplicity specifications
   (list "\\[\\(.*\\)\\]" 
	 '(1 font-lock-function-name-face))
   ;; role names
   (list "\\[.*\\][ \t]*role[ \t]*\\(\\w+\\)\\b"
	 '(1 font-lock-variable-name-face))
   ;; association ends refer to types
   (list "\\(.*\\)\\[.*\\]" 
	 '(1 font-lock-type-face))
   ;; invariant,pre,post names
   (list "\\(inv\\|pre\\|post\\)\\W*\\(\\w+\\):" 
	 '(2 font-lock-variable-name-face))
   ;; string constants
   (list "'[^']+'" 
	 '(0 font-lock-string-face))
   )
  (list "\\.use\\'")
  nil
  "Generic mode for USE specification files.")

(define-generic-mode 'usecmd-generic-mode
  (list "--")
  nil
  (list
   ;; commands in cmd files
   (list "!\\(\\w*\\)" 
	 '(1 font-lock-function-name-face))
   ;; string constants
   (list "'[^']+'" 
	 '(0 font-lock-string-face))
   )
  (list "\\.cmd\\'")
  nil
  "Generic mode for USE command files.")
