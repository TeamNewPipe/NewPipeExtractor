package org.schabi.newpipe.extractor.utils.jsextractor;

import org.mozilla.javascript.Context;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.Stack;

/**
 * JavaScript lexer that is able to parse JavaScript code and return its
 * tokens.
 *
 * <p>
 * The algorithm for distinguishing between division operators and regex literals
 * was taken from the <a href="https://github.com/rusty-ecma/RESS/">RESS lexer</a>.
 * </p>
 */
public class Lexer {
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
        public final Token token;
        public final int lineno;

        MetaToken(final Token token, final int lineno) {
            this.token = token;
            this.lineno = lineno;
        }
    }

    private static class BraceMetaToken extends MetaToken {
        public final Brace brace;

        BraceMetaToken(final Token token, final int lineno, final Brace brace) {
            super(token, lineno);
            this.brace = brace;
        }
    }

    private static class ParenMetaToken extends MetaToken {
        public final Paren paren;

        ParenMetaToken(final Token token, final int lineno, final Paren paren) {
            super(token, lineno);
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

        boolean oneIs(final Token token) {
            return list[0] != null && list[0].token == token;
        }

        boolean twoIs(final Token token) {
            return list[1] != null && list[1].token == token;
        }

        boolean threeIs(final Token token) {
            return list[2] != null && list[2].token == token;
        }
    }

    /**
     * Parsed token, containing the token and its position in the input string
     */
    public static class ParsedToken {
        public final Token token;
        public final int start;
        public final int end;

        ParsedToken(final Token token, final int start, final int end) {
            this.token = token;
            this.start = start;
            this.end = end;
        }
    }

    private final TokenStream stream;
    private final LookBehind lastThree;
    private final Stack<Brace> braceStack;
    private final Stack<Paren> parenStack;

    /**
     * Create a new JavaScript lexer with the given source code
     *
     * @param js JavaScript code
     * @param languageVersion JavaScript version (from Rhino)
     */
    public Lexer(final String js, final int languageVersion) {
        stream = new TokenStream(js, 0, languageVersion);
        lastThree = new LookBehind();
        braceStack = new Stack<>();
        parenStack = new Stack<>();
    }

    /**
     * Create a new JavaScript lexer with the given source code
     *
     * @param js JavaScript code
     */
    public Lexer(final String js) {
        this(js, Context.VERSION_DEFAULT);
    }

    /**
     * Continue parsing and return the next token
     * @return next token
     * @throws ParsingException
     */
    public ParsedToken getNextToken() throws ParsingException {
        Token token = stream.nextToken();

        if ((token == Token.DIV || token == Token.ASSIGN_DIV) && isRegexStart()) {
            stream.readRegExp(token);
            token = Token.REGEXP;
        }

        final ParsedToken parsedToken = new ParsedToken(token, stream.tokenBeg, stream.tokenEnd);
        keepBooks(parsedToken);
        return parsedToken;
    }

    /**
     * Check if the parser is balanced (equal amount of open and closed parentheses and braces)
     * @return true if balanced
     */
    public boolean isBalanced() {
        return braceStack.isEmpty() && parenStack.isEmpty();
    }

    /**
     * Evaluate the token for possible regex start and handle updating the
     * `self.last_three`, `self.paren_stack` and `self.brace_stack`
     */
    void keepBooks(final ParsedToken parsedToken) throws ParsingException {
        if (parsedToken.token.isPunct) {
            switch (parsedToken.token) {
                case LP:
                    handleOpenParenBooks();
                    return;
                case LC:
                    handleOpenBraceBooks();
                    return;
                case RP:
                    handleCloseParenBooks(parsedToken.start);
                    return;
                case RC:
                    handleCloseBraceBooks(parsedToken.start);
                    return;
            }
        }
        if (parsedToken.token != Token.COMMENT) {
            lastThree.push(new MetaToken(parsedToken.token, stream.lineno));
        }
    }

    /**
     * Handle the book keeping when we find an `(`
     */
    void handleOpenParenBooks() {
        boolean funcExpr = false;
        if (lastThree.oneIs(Token.FUNCTION)) {
            funcExpr = lastThree.two() != null && checkForExpression(lastThree.two().token);
        } else if (lastThree.twoIs(Token.FUNCTION)) {
            funcExpr = lastThree.three() != null && checkForExpression(lastThree.three().token);
        }

        final boolean conditional = lastThree.one() != null
                && lastThree.one().token.isConditional();

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
            switch (lastThree.one().token) {
                case LP:
                case LC:
                case CASE:
                    isBlock = false;
                    break;
                case COLON:
                    isBlock = !braceStack.isEmpty() && braceStack.lastElement().isBlock;
                    break;
                case RETURN:
                case YIELD:
                case YIELD_STAR:
                    isBlock = lastThree.two() != null && lastThree.two().lineno != stream.lineno;
                    break;
                default:
                    isBlock = !lastThree.one().token.isOp;
            }
        }

        Paren paren = null;
        if (lastThree.one() instanceof ParenMetaToken && lastThree.one().token == Token.RP) {
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

    boolean checkForExpression(final Token token) {
        return token.isOp || token == Token.RETURN || token == Token.CASE;
    }

    /**
     * Detect if the `/` is the beginning of a regex or is division
     * <a href="https://github.com/sweet-js/sweet-core/wiki/design">see this for more details</a>
     *
     * @return isRegexStart
     */
    boolean isRegexStart() {
        if (lastThree.one() != null) {
            final Token t = lastThree.one().token;
            if (t.isKeyw) {
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
            } else if (t.isPunct) {
                return t != Token.RB;
            } else {
                return false;
            }
        }

        return true;
    }
}
