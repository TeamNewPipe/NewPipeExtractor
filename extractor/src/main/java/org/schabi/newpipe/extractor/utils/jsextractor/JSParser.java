package org.schabi.newpipe.extractor.utils.jsextractor;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Stack;

public class JSParser {
    private static final HashSet<Integer> OPERATORS = new HashSet<>(Arrays.asList(
            Token.ASSIGN, Token.ASSIGN_ADD, Token.ASSIGN_SUB, Token.ASSIGN_MUL,
            Token.ASSIGN_DIV, Token.ASSIGN_MOD, Token.ASSIGN_BITAND,
            Token.ASSIGN_BITOR, Token.ASSIGN_BITXOR, Token.ASSIGN_EXP,
            Token.ASSIGN_LSH, Token.ASSIGN_RSH, Token.ASSIGN_URSH,
            Token.COMMA,

            Token.ADD, Token.SUB, Token.MUL, Token.DIV, Token.MOD,
            Token.LSH, Token.RSH, Token.URSH, Token.BITAND, Token.BITOR,
            Token.BITXOR, Token.AND, Token.OR, Token.HOOK, Token.COLON,
            Token.INSTANCEOF, Token.IN, Token.SHEQ, Token.EQ, Token.GE,
            Token.LE, Token.GT, Token.LT, Token.NE, Token.SHNE,

            Token.INC, Token.DEC, Token.BITNOT, Token.NOT, Token.DELPROP,
            Token.VOID, Token.TYPEOF, Token.THROW, Token.NEW
    ));

    private static final HashSet<Integer> PUNCTUATORS = new HashSet<>(Arrays.asList(
            Token.ASSIGN, Token.ASSIGN_ADD, Token.ASSIGN_SUB, Token.ASSIGN_MUL,
            Token.ASSIGN_DIV, Token.ASSIGN_MOD, Token.ASSIGN_BITAND,
            Token.ASSIGN_BITOR, Token.ASSIGN_BITXOR, Token.ASSIGN_EXP,
            Token.ASSIGN_LSH, Token.ASSIGN_RSH, Token.ASSIGN_URSH,
            Token.COMMA,

            Token.ADD, Token.SUB, Token.MUL, Token.DIV, Token.MOD,
            Token.LSH, Token.RSH, Token.URSH, Token.BITAND, Token.BITOR,
            Token.BITXOR, Token.AND, Token.OR, Token.HOOK, Token.COLON,
            Token.SHEQ, Token.EQ, Token.GE,
            Token.LE, Token.GT, Token.LT, Token.NE, Token.SHNE,

            Token.LP, Token.RP, Token.LB, Token.RB, Token.LC, Token.RC,
            Token.COLON, Token.DOT, Token.ARROW
    ));

    private static final HashSet<Integer> KEYWORDS = new HashSet<>(Arrays.asList(
            Token.INSTANCEOF, Token.IN, Token.DELPROP, Token.TYPEOF, Token.VOID,
            Token.BREAK, Token.CASE, Token.CATCH, Token.RESERVED, Token.CONTINUE,
            Token.DEBUGGER, Token.DEFAULT, Token.DO, Token.ELSE, Token.EXPORT,
            Token.FINALLY, Token.FOR, Token.FUNCTION, Token.IF, Token.IMPORT,
            Token.LET, Token.NEW, Token.RETURN, Token.SWITCH, Token.THROW,
            Token.TRY, Token.VAR, Token.WHILE, Token.WITH, Token.NULL,
            Token.TRUE, Token.FALSE, Token.YIELD, Token.CONST, Token.THIS
    ));

    private static final HashSet<Integer> CONDITIONALS = new HashSet<>(Arrays.asList(
            Token.IF, Token.FOR, Token.WHILE, Token.WITH
    ));

    private static class Paren {
        public final boolean funcExpr;
        public final boolean conditional;

        Paren(final boolean funcExpr, final boolean conditional) {
            this.funcExpr = funcExpr;
            this.conditional = conditional;
        }
    }

    private static class Brace {
        public final boolean isBlock;
        public final Paren paren;

        Brace(final boolean isBlock, final Paren paren) {
            this.isBlock = isBlock;
            this.paren = paren;
        }
    }

    private static class MetaToken {
        public final int type;
        public final int lineno;

        MetaToken(final int type, final int lineno) {
            this.type = type;
            this.lineno = lineno;
        }

        boolean isOperator() {
            return OPERATORS.contains(this.type);
        }

        boolean isKeyword() {
            return KEYWORDS.contains(this.type);
        }

        boolean isPunctuator() {
            return PUNCTUATORS.contains(this.type);
        }
    }

    private static class BraceMetaToken extends MetaToken {
        public final Brace brace;

        BraceMetaToken(final int type, final int lineno, final Brace brace) {
            super(type, lineno);
            this.brace = brace;
        }
    }

    private static class ParenMetaToken extends MetaToken {
        public final Paren paren;

        ParenMetaToken(final int type, final int lineno, final Paren paren) {
            super(type, lineno);
            this.paren = paren;
        }
    }

    private static class LookBehind {
        private final MetaToken[] list;

        LookBehind() {
            list = new MetaToken[3];
        }

        void push(final MetaToken t) {
            MetaToken toShift = t;
            for (int i = 0; i < 3; i++) {
                final MetaToken tmp = list[i];
                list[i] = toShift;
                toShift = tmp;
            }
        }

        MetaToken one() {
            return list[0];
        }

        MetaToken two() {
            return list[1];
        }

        MetaToken three() {
            return list[2];
        }

        boolean oneIs(final int type) {
            return list[0] != null && list[0].type == type;
        }

        boolean twoIs(final int type) {
            return list[1] != null && list[1].type == type;
        }

        boolean threeIs(final int type) {
            return list[2] != null && list[2].type == type;
        }
    }

    static class Item {
        public final int token;
        public final int start;
        public final int end;

        Item(final int token, final int start, final int end) {
            this.token = token;
            this.start = start;
            this.end = end;
        }

        boolean isOperator() {
            return OPERATORS.contains(this.token);
        }

        boolean isKeyword() {
            return KEYWORDS.contains(this.token);
        }

        boolean isPunctuator() {
            return PUNCTUATORS.contains(this.token);
        }
    }

    private String original;
    private final TokenStream stream;
    private final LookBehind lastThree;
    private final Stack<Brace> braceStack;
    private final Stack<Paren> parenStack;

    public JSParser(final String js) {
        original = js;
        stream = new TokenStream(null, js, 0);
        lastThree = new LookBehind();
        braceStack = new Stack<>();
        parenStack = new Stack<>();
    }

    public Item getNextToken() throws ParsingException, IOException {
        int type = stream.nextToken();

        if ((type == Token.DIV || type == Token.ASSIGN_DIV) && isRegexStart()) {
            stream.readRegExp(type);
            type = Token.REGEXP;
        }


        final Item item = new Item(type, stream.tokenBeg, stream.tokenEnd);
        keepBooks(item);
        return item;
    }

    public boolean isBalanced() {
        return braceStack.isEmpty() && parenStack.isEmpty();
    }

    /**
     * Evaluate the token for possible regex start and handle updating the
     * `self.last_three`, `self.paren_stack` and `self.brace_stack`
     */
    void keepBooks(final Item item) throws ParsingException {
        if (item.isPunctuator()) {
            switch (item.token) {
                case Token.LP:
                    handleOpenParenBooks();
                    return;
                case Token.LC:
                    handleOpenBraceBooks();
                    return;
                case Token.RP:
                    handleCloseParenBooks(item.start);
                    return;
                case Token.RC:
                    handleCloseBraceBooks(item.start);
                    return;
            }
        }
        if (item.token != Token.COMMENT) {
            lastThree.push(new MetaToken(item.token, stream.lineno));
        }
    }

    /**
     * Handle the book keeping when we find an `(`
     */
    void handleOpenParenBooks() {
        boolean funcExpr = false;
        if (lastThree.oneIs(Token.FUNCTION)) {
            funcExpr = lastThree.two() != null && checkForExpression(lastThree.two().type);
        } else if (lastThree.twoIs(Token.FUNCTION)) {
            funcExpr = lastThree.three() != null && checkForExpression(lastThree.three().type);
        }

        final boolean conditional = lastThree.one() != null
                && CONDITIONALS.contains(lastThree.one().type);

        final Paren paren = new Paren(funcExpr, conditional);
        parenStack.push(paren);
        lastThree.push(new ParenMetaToken(Token.LP, stream.lineno, paren));
    }

    /**
     * Handle the book keeping when we find an `{`
     */
    void handleOpenBraceBooks() {
        boolean isBlock = true;
        if (lastThree.one() != null) {
            switch (lastThree.one().type) {
                case Token.LP:
                case Token.LC:
                case Token.CASE:
                    isBlock = false;
                    break;
                case Token.COLON:
                    isBlock = !braceStack.isEmpty() && braceStack.lastElement().isBlock;
                    break;
                case Token.RETURN:
                case Token.YIELD:
                case Token.YIELD_STAR:
                    isBlock = lastThree.two() != null && lastThree.two().lineno != stream.lineno;
                    break;
                default:
                    isBlock = !OPERATORS.contains(lastThree.one().type);
            }
        }

        Paren paren = null;
        if (lastThree.one() instanceof ParenMetaToken && lastThree.one().type == Token.RP) {
            paren = ((ParenMetaToken) lastThree.one()).paren;
        }
        final Brace brace = new Brace(isBlock, paren);
        braceStack.push(brace);
        lastThree.push(new BraceMetaToken(Token.LC, stream.lineno, brace));
    }

    /**
     * Handle the book keeping when we find an `)`
     */
    void handleCloseParenBooks(final int start) throws ParsingException {
        if (parenStack.isEmpty()) {
            throw new ParsingException("unmached closing paren at " + start);
        }
        lastThree.push(new ParenMetaToken(Token.RP, stream.lineno, parenStack.pop()));
    }

    /**
     * Handle the book keeping when we find an `}`
     */
    void handleCloseBraceBooks(final int start) throws ParsingException {
        if (braceStack.isEmpty()) {
            throw new ParsingException("unmatched closing brace at " + start);
        }
        lastThree.push(new BraceMetaToken(Token.RC, stream.lineno, braceStack.pop()));
    }

    boolean checkForExpression(final int type) {
        return OPERATORS.contains(type) || type == Token.RETURN || type == Token.CASE;
    }

    /**
     * Detect if the `/` is the beginning of a regex or is division
     * <a href="https://github.com/sweet-js/sweet-core/wiki/design">see this for more details</a>
     *
     * @return isRegexStart
     */
    boolean isRegexStart() {
        if (lastThree.one() != null) {
            final int t = lastThree.one().type;
            if (KEYWORDS.contains(t)) {
                return t != Token.THIS;
            } else if (t == Token.RP && lastThree.one() instanceof ParenMetaToken) {
                return ((ParenMetaToken) lastThree.one()).paren.conditional;
            } else if (t == Token.RC && lastThree.one() instanceof BraceMetaToken) {
                final BraceMetaToken mt = (BraceMetaToken) lastThree.one();
                if (mt.brace.isBlock) {
                    if (mt.brace.paren != null) {
                        return !mt.brace.paren.funcExpr;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            } else if (PUNCTUATORS.contains(t)) {
                return t != Token.RB;
            }
        }

        return true;
    }
}
