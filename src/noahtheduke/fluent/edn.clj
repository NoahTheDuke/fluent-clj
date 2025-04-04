; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with resource
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.fluent.edn
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
    Junk
    Literal$NumberLiteral
    Literal$StringLiteral
    Message
    NamedArgument
    Pattern
    PatternElement$Placeable
    PatternElement$TextElement
    SelectExpression
    Term
    Variant)
   (fluent.syntax.parser ParseException ParseException$ErrorCode)
   (java.lang.reflect Method Field)
   (java.util Optional)))

(set! *warn-on-reflection* true)

(defn ^:private to-edn-dispatch [resource] (type resource))

(defmulti -to-edn
  {:arglists '([resource])}
  #'to-edn-dispatch)

(defn fluent->edn
  "Convert a Fluent object to a nested map."
  [resource]
  (-to-edn resource))

(defmethod -to-edn :default
  [resource]
  (throw (ex-info "I don't know what this is" {:resource resource})))

(defmethod -to-edn String [resource] resource)
(defmethod -to-edn Boolean [resource] resource)
(defmethod -to-edn Integer [resource] resource)

(defmethod -to-edn Optional
  [resource]
  (when-let [res (Optional/.orElse resource nil)]
    (-to-edn res)))

(defmethod -to-edn ParseException$ErrorCode
  [resource]
  {:type :exception/error-code
   :code (-to-edn (Enum/.name resource))})

(defmethod -to-edn ParseException
  [resource]
  (let [ch (.getDeclaredField ParseException "ch")
        arg (.getDeclaredField ParseException "arg")]
    (Field/.setAccessible ch true)
    (Field/.setAccessible arg true)
    {:type :exception/parse
     :error-code (-to-edn (ParseException/.errorCode resource))
     :arg (-to-edn (Field/.get arg resource))
     :line (-to-edn (ParseException/.line resource))
     :ch (-to-edn (Field/.get ch resource))}))

(defmethod -to-edn Junk
  [resource]
  {:type :ast/junk
   :content (-to-edn (Junk/.content resource))})

(defmethod -to-edn Commentary$Comment
  [resource]
  {:type :ast/comment
   :text (-to-edn (Commentary$Comment/.text resource))})

(defmethod -to-edn Commentary$GroupComment
  [resource]
  {:type :ast/group-comment
   :text (-to-edn (Commentary$GroupComment/.text resource))})

(defmethod -to-edn Commentary$ResourceComment
  [resource]
  {:type :ast/resource-comment
   :text (-to-edn (Commentary$ResourceComment/.text resource))})

(defmethod -to-edn Literal$StringLiteral
  [resource]
  {:type :ast/string-literal
   :text (-to-edn (Literal$StringLiteral/.value resource))})

(defmethod -to-edn Literal$NumberLiteral
  [resource]
  {:type :ast/number-literal
   :text (-to-edn (Literal$NumberLiteral/.value resource))})

(defmethod -to-edn Identifier
  [resource]
  {:type :ast/identifier
   :key (-to-edn (Identifier/.key resource))})

(defmethod -to-edn InlineExpression$MessageReference
  [resource]
  {:type :ast/message-reference
   :identifier (-to-edn (InlineExpression$MessageReference/.identifier resource))
   :attribute (-to-edn (InlineExpression$MessageReference/.attributeID resource))})

(defmethod -to-edn CallArguments
  [resource]
  {:type :ast/arguments
   :positional-args (mapv -to-edn (CallArguments/.positional resource))
   :named-args (mapv -to-edn (CallArguments/.named resource))})

(defmethod -to-edn InlineExpression$TermReference
  [resource]
  {:type :ast/term-reference 
   :identifier (-to-edn (InlineExpression$TermReference/.identifier resource))
   :attribute (-to-edn (InlineExpression$TermReference/.attributeID resource))
   :arguments (-to-edn (InlineExpression$TermReference/.arguments resource))})

(defmethod -to-edn InlineExpression$FunctionReference
  [resource]
  {:type :ast/function-reference
   :identifier (-to-edn (InlineExpression$FunctionReference/.identifier resource))
   :arguments (-to-edn (InlineExpression$FunctionReference/.arguments resource))})

(defmethod -to-edn NamedArgument
  [resource]
  {:type :ast/named-argument
   :name (-to-edn (NamedArgument/.name resource))
   :value (-to-edn (NamedArgument/.value resource))})

(defmethod -to-edn InlineExpression$VariableReference
  [resource]
  {:type :ast/variable-reference
   :identifier (-to-edn (InlineExpression$VariableReference/.identifier resource))})

(defmethod -to-edn Variant
  [resource]
  {:type :ast/variant
   :default (-to-edn (Variant/.isDefault resource))
   :key (-to-edn (Variant/.keyable resource))
   :value (-to-edn (Variant/.value resource))})

(defmethod -to-edn SelectExpression
  [resource]
  {:type :ast/select-expression
   :selector (-to-edn (SelectExpression/.selector resource))
   :variants (mapv -to-edn (SelectExpression/.variants resource))})

(defmethod -to-edn PatternElement$TextElement
  [resource]
  {:type :ast/text-element
   :value (-to-edn (PatternElement$TextElement/.value resource))})

(defmethod -to-edn PatternElement$Placeable
  [resource]
  {:type :ast/placeable
   :expression (-to-edn (PatternElement$Placeable/.expression resource))})

(defmethod -to-edn Pattern
  [resource]
  {:type :ast/pattern
   :elements (mapv -to-edn (Pattern/.elements resource))})

(defmethod -to-edn Attribute
  [resource]
  {:type :ast/attribute
   :identifier (-to-edn (Attribute/.identifier resource))
   :pattern (-to-edn (Attribute/.pattern resource))})

(defmethod -to-edn Term
  [resource]
  {:type :ast/term
   :comment (-to-edn (Term/.comment resource))
   :identifier (-to-edn (Term/.identifier resource))
   :value (-to-edn (Term/.value resource))
   :attributes (mapv -to-edn (Term/.attributes resource))})

(defmethod -to-edn Message
  [resource]
  {:type :ast/message
   :comment (-to-edn (Message/.comment resource))
   :identifier (-to-edn (Message/.identifier resource))
   :pattern (-to-edn (Message/.pattern resource))
   :attributes (mapv -to-edn (Message/.attributes resource))})

(defmethod -to-edn FluentResource
  [resource]
  {:type :ast/resource
   :entries (mapv -to-edn (FluentResource/.entries resource))
   :errors (mapv -to-edn (FluentResource/.errors resource))
   :junk (mapv -to-edn (FluentResource/.junk resource))})

;;;;;;; FROM EDN TO FLUENT

(defn ^:private from-edn-dispatch [resource] (or (:type resource)
                                                 (type resource)))

(defmulti -from-edn #'from-edn-dispatch)

(defn edn->fluent
  "Convert a map to a Fluent object."
  [resource-map]
  (-from-edn resource-map))

(defmethod -from-edn :default
  [resource]
  (throw (ex-info "I don't know what this is" {:resource resource})))

(defmethod -from-edn nil [_resource] nil)
(defmethod -from-edn String [resource] resource)
(defmethod -from-edn Boolean [resource] resource)
(defmethod -from-edn Integer [resource] resource)

(defmethod -from-edn :exception/parse
  [resource]
  (let [create (.getDeclaredMethod ParseException "create" (into-array [ParseException$ErrorCode String Integer/TYPE String]))]
    (Method/.setAccessible create true)
    (Method/.invoke create
                    ParseException
                    (into-array
                     Object
                     [(-from-edn (:error-code resource))
                      (-from-edn (:arg resource ""))
                      (-from-edn (:line resource))
                      (-from-edn (:ch resource))]))))

(defmethod -from-edn :exception/error-code
  [resource]
  (ParseException$ErrorCode/valueOf (-from-edn (:code resource))))

(defmethod -from-edn :ast/junk
  [resource]
  (Junk/new (-from-edn (:content resource))))

(defmethod -from-edn :ast/comment
  [resource]
  (Commentary$Comment/new (-from-edn (:text resource))))

(defmethod -from-edn :ast/group-comment
  [resource]
  (Commentary$GroupComment/new (Commentary$Comment/new (-from-edn (:text resource)))))

(defmethod -from-edn :ast/resource-comment
  [resource]
  (Commentary$ResourceComment/new (Commentary$Comment/new (-from-edn (:text resource)))))

(defmethod -from-edn :ast/string-literal
  [resource]
  (Literal$StringLiteral/of (-from-edn (:value resource))))

(defmethod -from-edn :ast/number-literal
  [resource]
  (Literal$NumberLiteral/from (-from-edn (:value resource))))

(defmethod -from-edn :ast/identifier
  [resource]
  (Identifier/new (-from-edn (:key resource))))

(defmethod -from-edn :ast/message-reference
  [resource]
  (InlineExpression$MessageReference/new
   (-from-edn (:identifier resource))
   (-from-edn (:attribute resource))))

(defmethod -from-edn :ast/arguments
  [resource]
  (CallArguments/new
   (mapv -from-edn (:positional-args resource))
   (mapv -from-edn (:named-args resource))))

(defmethod -from-edn :ast/term-reference
  [resource]
  (InlineExpression$TermReference/new
   (-from-edn (:identifier resource))
   (-from-edn (:attribute resource))
   (-from-edn (:arguments resource))))

(defmethod -from-edn :ast/function-reference
  [resource]
  (InlineExpression$FunctionReference/new
   (-from-edn (:identifier resource))
   (-from-edn (:arguments resource))))

(defmethod -from-edn :ast/named-argument
  [resource]
  (NamedArgument/new (-from-edn (:name resource))
                     (-from-edn (:value resource))))

(defmethod -from-edn :ast/variable-reference
  [resource]
  (InlineExpression$VariableReference/new
   (-from-edn (:identifier resource))))

(defmethod -from-edn :ast/variant
  [resource]
  (Variant/new
   (-from-edn (:key resource))
   (-from-edn (:value resource))
   (-from-edn (:default resource))))

(defmethod -from-edn :ast/select-expression
  [resource]
  (SelectExpression/new
   (-from-edn (:selector resource))
   (mapv -from-edn (:variants resource))))

(defmethod -from-edn :ast/text-element
  [resource]
  (PatternElement$TextElement/new
   (-from-edn (:value resource))))

(defmethod -from-edn :ast/placeable
  [resource]
  (PatternElement$Placeable/new
   (-from-edn (:expression resource))))

(defmethod -from-edn :ast/pattern
  [resource]
  (Pattern/new
   (mapv -from-edn (:elements resource))))

(defmethod -from-edn :ast/attribute
  [resource]
  (Attribute/new
   (-from-edn (:identifier resource))
   (-from-edn (:pattern resource))))

(defmethod -from-edn :ast/term
  [resource]
  (Term/new
   (-from-edn (:identifier resource))
   (-from-edn (:value resource))
   (mapv -from-edn (:attributes resource))
   (-from-edn (:comment resource))))

(defmethod -from-edn :ast/message
  [resource]
  (Message/new
   (-from-edn (:identifier resource))
   (-from-edn (:pattern resource))
   (mapv -from-edn (:attributes resource))
   (-from-edn (:comment resource))))

(defmethod -from-edn :ast/resource
  [resource]
  (FluentResource/new
   (mapv -from-edn (:entries resource))
   (mapv -from-edn (:errors resource))
   (mapv -from-edn (:junk resource))))

(comment
  (require '[noahtheduke.fluent.impl.clojure :refer [parse]]
           '[clj-memory-meter.core :as mm])
  (let [s "first = 1\n## cool comment time\nsecond = two\nthird = {$cnt ->\n    [one] {$cnt} Credit\n    *[others] {$cnt} Credits\n}"
        parsed (parse s)
        f (slurp "corpus/Russian_ru.ftl")
        f-parsed (parse f)]
    ; (prn f-parsed)
    ; (prn (fluent->edn f-parsed))
    (assert (= (fluent->edn parsed)
               (fluent->edn (edn->fluent (fluent->edn parsed)))
               (fluent->edn (edn->fluent (fluent->edn (edn->fluent (fluent->edn parsed)))))))
    ))
