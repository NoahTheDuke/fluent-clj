// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package noahtheduke.fluent;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fluent.bundle.FluentResource;
import fluent.syntax.AST.Attribute;
import fluent.syntax.AST.Commentary;
import fluent.syntax.AST.Entry;
import fluent.syntax.AST.Identifier;
import fluent.syntax.AST.InlineExpression;
import fluent.syntax.AST.Literal;
import fluent.syntax.AST.Message;
import fluent.syntax.AST.NamedArgument;
import fluent.syntax.AST.Pattern;
import fluent.syntax.AST.PatternElement;
import fluent.syntax.AST.SelectExpression;
import fluent.syntax.AST.SyntaxNode;
import fluent.syntax.AST.Term;
import fluent.syntax.AST.Variant;

class Context {
    public Writer out;
    public int indent;
    public boolean args;

    Context(Writer out) {
        this.out = out;
        this.indent = 0;
        this.args = false;
    }

    void write(String in) {
        try {
            this.out.write(in);
        } catch (Exception e) { }
    }

    void newline() {
        write("\n");
    }

    void appendIndent() {
        appendIndent(indent);
    }

    void appendIndent(int indent) {
        write("    ".repeat(indent));
    }

    void dedent() {
        if (this.indent > 0)
            this.indent--;
    }
};

public class FluentFmt {
    public static String printToString(Object resource) {
        Context ctx = new Context(new StringWriter());
        builderPrint(resource, ctx);
        return ctx.out.toString().stripTrailing();
    }

    static void builderPrint(Object resource, Context ctx) {
        Printer h = dispatch.get(resource.getClass());
        if (h == null) {
            throw new IllegalStateException("Didn't implement it for " + resource.getClass().getName() + "!!!");
        }
        h.printImpl(resource, ctx);
    }

    public static void print(Object resource) {
        System.out.println(printToString(resource));
    }

    private interface Printer {
        void printImpl(Object o, Context ctx);
    };

    private static Map<Class, Printer> dispatch = new HashMap<Class, Printer>();

    static {
        dispatch.put(Commentary.Comment.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                Commentary.Comment resource = (Commentary.Comment) o;
                ctx.newline();
                ctx.write("# ");
                ctx.write(resource.text());
            }
        });

        dispatch.put(Commentary.GroupComment.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                Commentary.GroupComment resource = (Commentary.GroupComment) o;
                ctx.newline();
                ctx.write("## ");
                ctx.write(resource.text());
            }
        });

        dispatch.put(Commentary.ResourceComment.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                Commentary.ResourceComment resource = (Commentary.ResourceComment) o;
                ctx.newline();
                ctx.write("### ");
                ctx.write(resource.text());
            }
        });

        dispatch.put(Literal.StringLiteral.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                Literal.StringLiteral resource = (Literal.StringLiteral) o;
                ctx.write("\"");
                ctx.write(resource.value());
                ctx.write("\"");
            }
        });

        dispatch.put(Identifier.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                Identifier resource = (Identifier) o;
                ctx.write(resource.key());
            }
        });

        dispatch.put(InlineExpression.MessageReference.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                InlineExpression.MessageReference resource = (InlineExpression.MessageReference) o;
                builderPrint(resource.identifier(), ctx);
            }
        });

        dispatch.put(InlineExpression.TermReference.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                InlineExpression.TermReference resource = (InlineExpression.TermReference) o;
                ctx.write("-");
                builderPrint(resource.identifier(), ctx);
                resource.attributeID().ifPresent(attr -> builderPrint(attr, ctx));
                resource.arguments().ifPresent(args -> {
                    ctx.write("(");
                    int idx = 0;
                    ctx.args = true;
                    List<SyntaxNode> both = Stream.concat(args.positional().stream(), args.named().stream())
                        .collect(Collectors.toList());
                    for (SyntaxNode arg : both) {
                        if (idx != 0) ctx.write(", ");
                        idx++;
                        builderPrint(arg, ctx);
                    }
                    ctx.args = false;
                    ctx.write(")");
                });
            }
        });

        dispatch.put(InlineExpression.FunctionReference.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                InlineExpression.FunctionReference resource = (InlineExpression.FunctionReference) o;
                builderPrint(resource.identifier(), ctx);
                resource.arguments().ifPresent(args -> {
                    ctx.write(" (");
                    int idx = 0;
                    ctx.args = true;
                    List<SyntaxNode> both = Stream.concat(args.positional().stream(), args.named().stream())
                        .collect(Collectors.toList());
                    for (SyntaxNode pos : both) {
                        if (idx != 0) ctx.write(", ");
                        builderPrint(pos, ctx);
                    }
                    ctx.args = false;
                    ctx.write(")");
                });
            }
        });

        dispatch.put(InlineExpression.VariableReference.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                InlineExpression.VariableReference resource = (InlineExpression.VariableReference) o;
                ctx.write("$");
                builderPrint(resource.identifier(), ctx);
            }
        });

        dispatch.put(NamedArgument.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                NamedArgument resource = (NamedArgument) o;
                builderPrint(resource.name(), ctx);
                ctx.write(": ");
                builderPrint(resource.value(), ctx);
            }
        });

        dispatch.put(Variant.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                Variant resource = (Variant) o;
                if (resource.isDefault()) {
                    ctx.write("*");
                }
                ctx.write("[");
                builderPrint(resource.keyable(), ctx);
                ctx.write("] ");
                builderPrint(resource.value(), ctx);
            }
        });

        dispatch.put(SelectExpression.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                SelectExpression resource = (SelectExpression) o;
                builderPrint(resource.selector(), ctx);
                ctx.write(" ->\n");
                for (Variant variant : resource.variants()) {
                    ctx.appendIndent();
                    builderPrint(variant, ctx);
                    ctx.newline();
                }
                int indent = Math.max(ctx.indent - 1, 0);
                ctx.appendIndent(indent);
            }
        });

        dispatch.put(PatternElement.TextElement.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                PatternElement.TextElement resource = (PatternElement.TextElement) o;
                String s = resource.value();
                ctx.write(s);
                if (s.contains("\n")) {
                    ctx.appendIndent();
                }
            }
        });

        dispatch.put(PatternElement.Placeable.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                PatternElement.Placeable resource = (PatternElement.Placeable) o;
                ctx.write("{");
                builderPrint(resource.expression(), ctx);
                ctx.write("}");
            }
        });

        dispatch.put(Pattern.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                Pattern resource = (Pattern) o;
                ctx.indent++;
                for (PatternElement pat : resource.elements()) {
                    builderPrint(pat, ctx);
                }
                ctx.dedent();
            }
        });

        dispatch.put(Attribute.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                Attribute resource = (Attribute) o;
                ctx.indent++;
                ctx.appendIndent();
                ctx.write(".");
                builderPrint(resource.identifier(), ctx);
                ctx.write(" = ");
                builderPrint(resource.pattern(), ctx);
                ctx.newline();
                ctx.dedent();
            }
        });

        dispatch.put(Term.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                Term resource = (Term) o;
                resource.comment().ifPresent(cmnt -> {
                    builderPrint(cmnt, ctx);
                    ctx.newline();
                });
                ctx.write("-");
                builderPrint(resource.identifier(), ctx);
                ctx.write(" = ");
                builderPrint(resource.value(), ctx);
                List<Attribute> attrs = resource.attributes();
                if (attrs.size() > 0) {
                    ctx.newline();
                    for (Attribute attr : attrs) {
                        builderPrint(attr, ctx);
                    }
                }
                ctx.newline();
            }
        });

        dispatch.put(Message.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                Message resource = (Message) o;
                resource.comment().ifPresent(cmnt -> {
                    builderPrint(cmnt, ctx);
                    ctx.newline();
                });
                builderPrint(resource.identifier(), ctx);
                ctx.write(" = ");
                resource.pattern().ifPresent(pat -> builderPrint(pat, ctx));
                List<Attribute> attrs = resource.attributes();
                if (attrs.size() > 0) {
                    ctx.newline();
                    for (Attribute attr : attrs) {
                        builderPrint(attr, ctx);
                    }
                }
                ctx.newline();
            }
        });

        dispatch.put(FluentResource.class, new Printer() {
            public void printImpl(Object o, Context ctx) {
                FluentResource resource = (FluentResource) o;
                for (Entry entry : resource.entries()) {
                    builderPrint(entry, ctx);
                }
            }
        });
    }
}
