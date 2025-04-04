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

(defrecord Context [^Writer out ^int indent])

(defprotocol FTLPrinter
  (-pprint [this ctx]))

(defn pprint
  "Format and print a Fluent object to *out*."
  [resource]
  (-pprint resource (Context/new *out* 0)))

(defn pprint-str
  "Format and print a Fluent object to a string."
  [resource]
  (let [out (StringWriter/new)]
    (-pprint resource (Context/new out 0))
    (String/.trim (StringWriter/.toString out))))

(defn write
  [ctx ^String s]
  (Writer/.write (:out ctx) s))

(defn indent
  [^Context ctx]
  (write ctx (String/.repeat "    " (:indent ctx))))

(extend-protocol FTLPrinter
  Commentary$Comment
  (-pprint
   [this ctx]
   (write ctx "# ")
   (write ctx (Commentary$Comment/.text this))
   (write ctx "\n\n"))

  Commentary$GroupComment
  (-pprint
   [this ctx]
   (write ctx "\n")
   (write ctx "## ")
   (write ctx (Commentary$GroupComment/.text this))
   (write ctx "\n\n"))

  Commentary$ResourceComment
  (-pprint
   [this ctx]
   (write ctx "\n")
   (write ctx "### ")
   (write ctx (Commentary$ResourceComment/.text this))
   (write ctx "\n\n"))

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
   (-pprint (InlineExpression$MessageReference/.identifier this) ctx))

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
            (run! (fn [arg] (if (string? arg)
                              (write ctx arg)
                              (-pprint arg ctx)))))
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
            (run! (fn [arg] (if (string? arg)
                              (write ctx arg)
                              (-pprint arg ctx)))))
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
   (when (Variant/.isDefault this)
     (write ctx "*"))
   (write ctx "[")
   (-pprint (Variant/.keyable this) ctx)
   (write ctx "] ")
   (-pprint (Variant/.value this) ctx))

  SelectExpression
  (-pprint
   [this ctx]
   (-pprint (SelectExpression/.selector this) ctx)
   (write ctx " ->")
   (write ctx "\n")
   (run! (fn [variant]
           (indent ctx)
           (-pprint variant ctx)
           (write ctx "\n"))
         (SelectExpression/.variants this))
   (let [ctx (update ctx :indent #(max 0 (unchecked-dec %)))]
     (indent ctx)))

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
   (write ctx "{")
   (-pprint (PatternElement$Placeable/.expression this) ctx)
   (write ctx "}"))

  Pattern
  (-pprint
   [this ctx]
   (let [ctx (update ctx :indent inc)]
     (run! (fn [pat] (-pprint pat ctx)) (Pattern/.elements this))))

  Attribute
  (-pprint
   [this ctx]
   (let [ctx (update ctx :indent inc)]
     (indent ctx)
     (write ctx ".")
     (-pprint (Attribute/.identifier this) ctx)
     (write ctx " = ")
     (-pprint (Attribute/.pattern this) ctx)
     (write ctx "\n")))

  Term
  (-pprint
   [this ctx]
   (Optional/.ifPresent (Term/.comment this)
                        (fn [cmnt]
                          (write ctx "\n# ")
                          (write ctx (Commentary$Comment/.text cmnt))
                          (write ctx "\n")))
   (write ctx "-")
   (-pprint (Term/.identifier this) ctx)
   (write ctx " = ")
   (-pprint (Term/.value this) ctx)
   (when-let [attrs (not-empty (Term/.attributes this))]
     (write ctx "\n")
     (run! (fn [attr] (-pprint attr ctx)) attrs))
   (write ctx "\n"))

  Message
  (-pprint
   [this ctx]
   (Optional/.ifPresent (Message/.comment this)
                        (fn [cmnt]
                          (write ctx "\n# ")
                          (write ctx (Commentary$Comment/.text cmnt))
                          (write ctx "\n")))
   (-pprint (Message/.identifier this) ctx)
   (write ctx " = ")
   (Optional/.ifPresent (Message/.pattern this)
                        (fn [pat]
                          (-pprint pat ctx)))
   (when-let [attrs (not-empty (Message/.attributes this))]
     (write ctx "\n")
     (run! (fn [attr] (-pprint attr ctx)) attrs))
   (write ctx "\n"))

  FluentResource
  (-pprint
   [this ctx]
   (let [entries (FluentResource/.entries this)]
     (doseq [entry entries]
       (-pprint entry ctx)))))
