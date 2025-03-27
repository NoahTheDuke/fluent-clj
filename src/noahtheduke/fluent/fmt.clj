; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.fluent.fmt
  (:require
   [clojure.java.io :as io]
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
    Literal$StringLiteral
    Message
    NamedArgument
    Pattern
    PatternElement$Placeable
    PatternElement$TextElement
    SelectExpression
    Term
    Variant)
   (fluent.syntax.parser FTLParser FTLStream)
   (java.io StringWriter Writer)
   (java.util Optional)
   (noahtheduke.fluent FluentFmt)))

(set! *warn-on-reflection* true)

(defrecord Context [^Writer out ^int indent ^boolean args])

(defn write
  [ctx ^String s]
  (Writer/.write (:out ctx) s))

(defn indent
  [^Context ctx]
  (write ctx (String/.repeat "    " (:indent ctx))))

(defprotocol FTLPrinter
  (-ftl-print [this ctx]))

(extend-protocol FTLPrinter
  Commentary$Comment
  (-ftl-print
   [this ctx]
   (write ctx "\n")
   (write ctx "# ")
   (write ctx (Commentary$Comment/.text this)))

  Commentary$GroupComment
  (-ftl-print
   [this ctx]
   (write ctx "\n")
   (write ctx "## ")
   (write ctx (Commentary$GroupComment/.text this)))

  Commentary$ResourceComment
  (-ftl-print
   [this ctx]
   (write ctx "\n")
   (write ctx "### ")
   (write ctx (Commentary$ResourceComment/.text this)))

  Literal$StringLiteral
  (-ftl-print
   [this ctx]
   (write ctx "\"")
   (write ctx (Literal$StringLiteral/.value this))
   (write ctx "\""))

  Identifier
  (-ftl-print
   [this ctx]
   (write ctx (Identifier/.key this)))

  InlineExpression$MessageReference
  (-ftl-print
   [this ctx]
   (-ftl-print (InlineExpression$MessageReference/.identifier this) ctx))

  InlineExpression$TermReference
  (-ftl-print
   [this ctx]
   (write ctx "-")
   (-ftl-print (InlineExpression$TermReference/.identifier this) ctx)
   (when-let [attr (Optional/.orElse (InlineExpression$TermReference/.attributeID this) nil)]
     (-ftl-print attr ctx))
   (when-let [args (Optional/.orElse (InlineExpression$TermReference/.arguments this) nil)]
     (let [positional (CallArguments/.positional args)
           named (CallArguments/.named args)
           ctx (assoc ctx :args true)]
       (write ctx "(")
       (->> (concat positional named)
            (interpose ", ")
            (run! (fn [arg] (if (string? arg)
                              (write ctx arg)
                              (-ftl-print arg ctx)))))
       (write ctx ")"))))

  InlineExpression$FunctionReference
  (-ftl-print
   [this ctx]
   (-ftl-print (InlineExpression$FunctionReference/.identifier this) ctx)
   (when-let [args (Optional/.orElse (InlineExpression$FunctionReference/.arguments this) nil)]
     (let [positional (CallArguments/.positional args)
           named (CallArguments/.named args)
           ctx (assoc ctx :args true)]
       (write ctx "(")
       (->> (concat positional named)
            (interpose ", ")
            (run! (fn [arg] (if (string? arg)
                              (write ctx arg)
                              (-ftl-print arg ctx)))))
       (write ctx ")"))))

  NamedArgument
  (-ftl-print
   [this ctx]
   (-ftl-print (NamedArgument/.name this) ctx)
   (write ctx ": ")
   (-ftl-print (NamedArgument/.value this) ctx))

  InlineExpression$VariableReference
  (-ftl-print
   [this ctx]
   (write ctx "$")
   (-ftl-print (InlineExpression$VariableReference/.identifier this) ctx))

  Variant
  (-ftl-print
   [this ctx]
   (when (Variant/.isDefault this)
     (write ctx "*"))
   (write ctx "[")
   (-ftl-print (Variant/.keyable this) ctx)
   (write ctx "] ")
   (-ftl-print (Variant/.value this) ctx))

  SelectExpression
  (-ftl-print
   [this ctx]
   (-ftl-print (SelectExpression/.selector this) ctx)
   (write ctx " ->")
   (write ctx "\n")
   (run! (fn [variant]
           (indent ctx)
           (-ftl-print variant ctx)
           (write ctx "\n"))
         (SelectExpression/.variants this))
   (let [ctx (update ctx :indent #(max 0 (unchecked-dec %)))]
     (indent ctx)))

  PatternElement$TextElement
  (-ftl-print
   [this ctx]
   (let [s (PatternElement$TextElement/.value this)]
     (write ctx s)
     (when (str/includes? s "\n")
       (indent ctx))))

  PatternElement$Placeable
  (-ftl-print
   [this ctx]
   (write ctx "{")
   (-ftl-print (PatternElement$Placeable/.expression this) ctx)
   (write ctx "}"))

  Pattern
  (-ftl-print
   [this ctx]
   (let [ctx (update ctx :indent inc)]
     (run! (fn [pat] (-ftl-print pat ctx)) (Pattern/.elements this))))

  Attribute
  (-ftl-print
   [this ctx]
   (let [ctx (update ctx :indent inc)]
     (indent ctx)
     (write ctx ".")
     (-ftl-print (Attribute/.identifier this) ctx)
     (write ctx " = ")
     (-ftl-print (Attribute/.pattern this) ctx)
     (write ctx "\n")))

  Term
  (-ftl-print
   [this ctx]
   (Optional/.ifPresent (Term/.comment this)
                        (fn [cmnt]
                          (-ftl-print cmnt ctx)
                          (write ctx "\n")))
   (write ctx "-")
   (-ftl-print (Term/.identifier this) ctx)
   (write ctx " = ")
   (-ftl-print (Term/.value this) ctx)
   (when-let [attrs (not-empty (Term/.attributes this))]
     (write ctx "\n")
     (run! (fn [attr] (-ftl-print attr ctx)) attrs))
   (write ctx "\n"))

  Message
  (-ftl-print
   [this ctx]
   (Optional/.ifPresent (Message/.comment this)
                        (fn [cmnt]
                          (-ftl-print cmnt ctx)
                          (write ctx "\n")))
   (-ftl-print (Message/.identifier this) ctx)
   (write ctx " = ")
   (Optional/.ifPresent (Message/.pattern this)
                        (fn [pat]
                          (-ftl-print pat ctx)))
   (when-let [attrs (not-empty (Message/.attributes this))]
     (write ctx "\n")
     (run! (fn [attr] (-ftl-print attr ctx)) attrs))
   (write ctx "\n"))

  FluentResource
  (-ftl-print
   [this ctx]
   (let [entries (FluentResource/.entries this)]
     (doseq [entry entries]
       (-ftl-print entry ctx)))))

(defn fmt->string
  "Format and print to a string"
  [resource]
  (let [out (StringWriter/new)]
    (-ftl-print resource (->Context out 0 false))
    (str/trim (str out))))

(defn fmt->print
  "Format and print to *out*"
  [resource]
  (-ftl-print resource (->Context *out* 0 false)))

(defn fmt->file
  "Format all files in a directory"
  [{:keys [dir]}]
  (doseq [f (->> (io/file dir)
                 (file-seq)
                 (filter #(.isFile ^java.io.File %))
                 (filter #(str/ends-with? (str %) ".ftl")))
          :let [contents (-> (slurp f)
                             (str/replace #"\\u([0-9A-F]{4})" "__FLUENT_CLJ__u$1")
                             (str/replace #"\\U([0-9A-F]{4})" "__FLUENT_CLJ__U$1"))
                ast (FTLParser/parse (FTLStream/of contents) false)
                formatted-file (-> (with-out-str (fmt->string ast))
                                   (str/replace #"__FLUENT_CLJ__u([0-9A-F]{4})" "\\\\u$1")
                                   (str/replace #"__FLUENT_CLJ__U([0-9A-F]{4})" "\\\\U$1")
                                   (str/trim)
                                   (str "\n"))]]
    (spit f formatted-file)))

(comment
  (fmt->file {:dir "corpus"}))

(comment
  (require '[criterium.core :as c])
  (let [f (slurp "corpus/Russian_ru.ftl")
        ast (FTLParser/parse (FTLStream/of f) false)]
    (assert (= (str/trim (with-out-str (fmt->print ast)))
               (str/trim (FluentFmt/printToString ast))))
    ; (spit "clj-fmt.ftl" (ftl-string ast))
    ; (spit "java-fmt.ftl" (FluentFmt/printToString ast))
    (println "clj print")
    (c/quick-bench (fmt->string ast))
    (println "java print")
    (c/quick-bench (FluentFmt/printToString ast))
    ))
