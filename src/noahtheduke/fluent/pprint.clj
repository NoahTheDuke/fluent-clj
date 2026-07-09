; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.fluent.pprint
  (:require
   [clojure.string :as str])
  (:import
   (fluent.bundle FluentResource)
   (fluent.syntax.AST
    Attribute
    CallArguments
    Commentary$Comment
    Commentary$GroupComment
    Commentary$ResourceComment
    Identifier
    InlineExpression$FunctionReference
    InlineExpression$MessageReference
    InlineExpression$TermReference
    InlineExpression$VariableReference
    Literal$DoubleLiteral
    Literal$LongLiteral
    Literal$StringLiteral
    Message
    NamedArgument
    Pattern
    PatternElement$Placeable
    PatternElement$TextElement
    SelectExpression
    Term
    Variant)
   (java.io StringWriter Writer)
   (java.util Optional)))

(set! *warn-on-reflection* true)

(defrecord Context [out ^int indent has-entries? sort?])

(defn write
  [ctx ^String s]
  (Writer/.write (:out ctx) s))

(defn indent
  [ctx]
  (write ctx (String/.repeat "    " (:indent ctx))))

(defprotocol FTLPrinter
  (-pprint [this ctx]))

(defn pprint
  "Format and print a Fluent object to *out*."
  ([resource] (pprint resource *out* nil))
  ([resource out] (pprint resource out nil))
  ([resource out {:keys [sort?]
                  :or {sort? false}}]
   (when-not (instance? Writer out)
     (throw (ex-info "Must provide a valid Writer out" {:out out})))
   (let [ctx (Context/new out 0 (volatile! false) sort?)]
     (-pprint resource ctx)
     (write ctx "\n"))))

(defn pprint-str
  "Format and print a Fluent object to a string."
  {:arglists '([resource] [resource {:keys [sort?], :or {sort? false}, :as args}])}
  ([resource] (pprint-str resource nil))
  ([resource args]
   (let [out (StringWriter/new)]
     (pprint resource out args)
     (String/.trim (StringWriter/.toString out)))))

(defn start-on-newline?
  [pattern]
  (let [elements (Pattern/.elements pattern)
        multiline? (some #(or (and (instance? PatternElement$Placeable %)
                                   (let [expression (PatternElement$Placeable/.expression %)]
                                     (instance? SelectExpression expression)))
                              (and (instance? PatternElement$TextElement %)
                                   (str/includes? (PatternElement$TextElement/.value %) "\n")))
                         elements)]
    (when multiline?
      (let [first-element (first elements)]
        (if (instance? PatternElement$TextElement first-element)
          (case (first (PatternElement$TextElement/.value first-element))
            (\[ \. \*) false
            #_:else true)
          true)))))

(defn write-comment
  [ctx prefix text]
  (if-let [text (not-empty text)]
    (let [output (->> (str/split-lines text)
                      (mapv #(str prefix " " %))
                      (str/join "\n"))]
      (when @(:has-entries? ctx)
        (write ctx "\n"))
      (write ctx output))
    (write ctx prefix))
  (write ctx "\n"))

(extend-protocol FTLPrinter
  Commentary$Comment
  (-pprint
   [this ctx]
   (write-comment ctx "#" (Commentary$Comment/.text this))
   (write ctx "\n"))

  Commentary$GroupComment
  (-pprint
   [this ctx]
   (write-comment ctx "##" (Commentary$GroupComment/.text this))
   (write ctx "\n"))

  Commentary$ResourceComment
  (-pprint
   [this ctx]
   (write-comment ctx "###" (Commentary$ResourceComment/.text this))
   (write ctx "\n"))

  Literal$DoubleLiteral
  (-pprint
   [this ctx]
   (write ctx (str (Literal$DoubleLiteral/.value this))))

  Literal$LongLiteral
  (-pprint
   [this ctx]
   (write ctx (str (Literal$LongLiteral/.value this))))

  Literal$StringLiteral
  (-pprint
   [this ctx]
   (write ctx "\"")
   (write ctx (Literal$StringLiteral/.value this))
   (write ctx "\""))

  Identifier
  (-pprint
   [this ctx]
   (write ctx (Identifier/.key this)))

  InlineExpression$MessageReference
  (-pprint
   [this ctx]
   (-pprint (InlineExpression$MessageReference/.identifier this) ctx)
   (Optional/.ifPresent (InlineExpression$MessageReference/.attributeID this)
                        (fn [element]
                          (write ctx ".")
                          (-pprint element ctx))))

  InlineExpression$TermReference
  (-pprint
   [this ctx]
   (write ctx "-")
   (-pprint (InlineExpression$TermReference/.identifier this) ctx)
   (when-let [attr (Optional/.orElse (InlineExpression$TermReference/.attributeID this) nil)]
     (write ctx ".")
     (-pprint attr ctx))
   (when-let [args (Optional/.orElse (InlineExpression$TermReference/.arguments this) nil)]
     (let [positional (CallArguments/.positional args)
           named (CallArguments/.named args)]
       (write ctx "(")
       (->> (concat positional named)
            (interpose ", ")
            (run! (fn [arg]
                    (if (string? arg)
                      (write ctx arg)
                      (-pprint arg ctx))
                    nil)))
       (write ctx ")"))))

  InlineExpression$FunctionReference
  (-pprint
   [this ctx]
   (-pprint (InlineExpression$FunctionReference/.identifier this) ctx)
   (when-let [args (Optional/.orElse (InlineExpression$FunctionReference/.arguments this) nil)]
     (let [positional (CallArguments/.positional args)
           named (CallArguments/.named args)]
       (write ctx "(")
       (->> (concat positional named)
            (interpose ", ")
            (run! (fn [arg]
                    (if (string? arg)
                      (write ctx arg)
                      (-pprint arg ctx))
                    nil)))
       (write ctx ")"))))

  NamedArgument
  (-pprint
   [this ctx]
   (-pprint (NamedArgument/.name this) ctx)
   (write ctx ": ")
   (-pprint (NamedArgument/.value this) ctx))

  InlineExpression$VariableReference
  (-pprint
   [this ctx]
   (write ctx "$")
   (-pprint (InlineExpression$VariableReference/.identifier this) ctx))

  Variant
  (-pprint
   [this ctx]
   (let [original ctx
         ctx (update ctx :indent inc)]
     (if (Variant/.isDefault this)
       (do (indent original)
           (write ctx "   *"))
       (indent ctx))
     (write ctx "[")
     (-pprint (Variant/.keyable this) ctx)
     (write ctx "]")
     (-pprint (Variant/.value this) ctx)))

  SelectExpression
  (-pprint
   [this ctx]
   (write ctx "{ ")
   (-pprint (SelectExpression/.selector this) ctx)
   (write ctx " ->")
   (write ctx "\n")
   ;; letting Variant determine indentation
   (run! (fn [variant]
           (-pprint variant ctx)
           (write ctx "\n")
           nil)
         (SelectExpression/.variants this))
   (indent ctx)
   (write ctx "}"))

  PatternElement$TextElement
  (-pprint
   [this ctx]
   (let [s (PatternElement$TextElement/.value this)]
     (write ctx s)
     (when (str/includes? s "\n")
       (indent ctx))))

  PatternElement$Placeable
  (-pprint
   [this ctx]
   (let [expr (PatternElement$Placeable/.expression this)]
     (cond
       (instance? PatternElement$Placeable expr)
       (do (write ctx "{")
           (-pprint expr ctx)
           (write ctx "}"))
       (instance? SelectExpression expr)
       (-pprint expr ctx)
       :else
       (do (write ctx "{ ")
           (-pprint expr ctx)
           (write ctx " }")))))

  Pattern
  (-pprint
   [this ctx]
   (when-let [elements (seq (Pattern/.elements this))]
     ; (binding [*err* *out*] (prn :pattern elements))
     (if (start-on-newline? this)
       (let [ctx (update ctx :indent inc)]
         (write ctx "\n")
         (indent ctx)
         ; (binding [*err* *out*] (prn :pattern-newline elements))
         (run! (fn [pat] (-pprint pat ctx) nil) elements))
       (let [elem (first elements)]
         (write ctx " ")
         (-pprint elem ctx)
         (when-let [elements (next elements)]
           ; (binding [*err* *out*] (prn :pattern-next elements))
           (let [contains-newline? (and (instance? PatternElement$TextElement elem)
                                        (str/includes? (PatternElement$TextElement/.value elem) "\n"))
                 ctx (update ctx :indent inc)]
             (run! (fn [pat]
                     (when contains-newline?
                       (indent ctx))
                     (-pprint pat ctx)
                     nil)
                   elements)))))))

  Attribute
  (-pprint
   [this ctx]
   (let [ctx (update ctx :indent inc)]
     (write ctx "\n")
     (indent ctx)
     (write ctx ".")
     (-pprint (Attribute/.identifier this) ctx)
     (write ctx " =")
     (-pprint (Attribute/.pattern this) ctx)))

  Term
  (-pprint
   [this ctx]
   (vreset! (:has-entries? ctx) true)
   (Optional/.ifPresent (Term/.comment this)
                        ;; don't use -pprint to avoid trailing "\n"
                        #(write-comment ctx "#" (Commentary$Comment/.text %)))
   (write ctx "-")
   (-pprint (Term/.identifier this) ctx)
   (write ctx " =")
   (-pprint (Term/.value this) ctx)
   (when-let [attrs (not-empty (Term/.attributes this))]
     (run! (fn [attr] (-pprint attr ctx) nil) attrs))
   (write ctx "\n"))

  Message
  (-pprint
   [this ctx]
   (vreset! (:has-entries? ctx) true)
   (Optional/.ifPresent (Message/.comment this)
                        ;; don't use -pprint to avoid trailing "\n"
                        #(write-comment ctx "#" (Commentary$Comment/.text %)))
   (-pprint (Message/.identifier this) ctx)
   (write ctx " =")
   (Optional/.ifPresent (Message/.pattern this) #(-pprint % ctx))
   (when-let [attrs (not-empty (Message/.attributes this))]
     (run! (fn [attr] (-pprint attr ctx) nil) attrs))
   (write ctx "\n"))

  FluentResource
  (-pprint
   [this ctx]
   (let [entries (if (:sort? ctx)
                   (let [msg-term? #(or (instance? Message %)
                                        (instance? Term %))
                         get-name (memoize #(cond
                                              (instance? Message %)
                                              (str/lower-case (str (Message/.identifier %)))
                                              (instance? Term %)
                                              (str/lower-case (str (Term/.identifier %)))))]
                     (eduction
                      (partition-by msg-term?)
                      (mapcat (fn [entries]
                                (if (msg-term? (first entries))
                                  (sort-by get-name entries)
                                  entries)))
                      (FluentResource/.entries this)))
                   (FluentResource/.entries this))]
     (run! (fn [entry] (-pprint entry ctx) nil) entries))))

(comment
  (require '[noahtheduke.fluent.impl.clojure :refer [parse]])
  (let [f (slurp "corpus/Russian_ru.ftl")
        parsed (parse f)]
    (assert (empty? (FluentResource/.errors (parse (pprint-str parsed))))
            (FluentResource/.errors (parse (pprint-str parsed))))
    ; (pprint-str parsed)
    ; (prn f-parsed)
    ; (prn (pprint-str f-parsed))
    (assert (= (pprint-str parsed)
               (pprint-str (parse (pprint-str parsed)))
               (pprint-str (parse (pprint-str (parse (pprint-str parsed)))))))
    ))
