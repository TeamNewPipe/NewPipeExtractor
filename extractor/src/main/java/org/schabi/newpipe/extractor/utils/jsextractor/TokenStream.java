package org.schabi.newpipe.extractor.utils.jsextractor;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.ObjToIntMap;
import org.mozilla.javascript.ScriptRuntime;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

/* Source: Mozilla Rhino, org.mozilla.javascript.Token
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * */
class TokenStream {
    /*
     * For chars - because we need something out-of-range
     * to check.  (And checking EOF by exception is annoying.)
     * Note distinction from EOF token type!
     */
    private static final int EOF_CHAR = -1;

    /*
     * Return value for readDigits() to signal the caller has
     * to return an number format problem.
     */
    private static final int REPORT_NUMBER_FORMAT_ERROR = -2;

    private static final char BYTE_ORDER_MARK = '\uFEFF';
    private static final char NUMERIC_SEPARATOR = '_';

    TokenStream(final String sourceString, final int lineno, final int languageVersion) {
        this.sourceString = sourceString;
        this.sourceCursor = 0;
        this.cursor = 0;

        this.lineno = lineno;
        this.languageVersion = languageVersion;
    }

    static boolean isKeyword(final String s, final int version, final boolean isStrict) {
        return Token.EOF != stringToKeyword(s, version, isStrict);
    }

    private static Token stringToKeyword(final String name, final int version,
                                       final boolean isStrict) {
        if (version < Context.VERSION_ES6) {
            return stringToKeywordForJS(name);
        }
        return stringToKeywordForES(name, isStrict);
    }

    /** JavaScript 1.8 and earlier */
    private static Token stringToKeywordForJS(final String name) {
        switch (name) {
            case "break":
                return Token.BREAK;
            case "case":
                return Token.CASE;
            case "continue":
                return Token.CONTINUE;
            case "default":
                return Token.DEFAULT;
            case "delete":
                return Token.DELPROP;
            case "do":
                return Token.DO;
            case "else":
                return Token.ELSE;
            case "export":
                return Token.EXPORT;
            case "false":
                return Token.FALSE;
            case "for":
                return Token.FOR;
            case "function":
                return Token.FUNCTION;
            case "if":
                return Token.IF;
            case "in":
                return Token.IN;
            case "let":
                return Token.LET;
            case "new":
                return Token.NEW;
            case "null":
                return Token.NULL;
            case "return":
                return Token.RETURN;
            case "switch":
                return Token.SWITCH;
            case "this":
                return Token.THIS;
            case "true":
                return Token.TRUE;
            case "typeof":
                return Token.TYPEOF;
            case "var":
                return Token.VAR;
            case "void":
                return Token.VOID;
            case "while":
                return Token.WHILE;
            case "with":
                return Token.WITH;
            case "yield":
                return Token.YIELD;
            case "throw":
                return Token.THROW;
            case "catch":
                return Token.CATCH;
            case "const":
                return Token.CONST;
            case "debugger":
                return Token.DEBUGGER;
            case "finally":
                return Token.FINALLY;
            case "instanceof":
                return Token.INSTANCEOF;
            case "try":
                return Token.TRY;
            case "abstract":
            case "boolean":
            case "byte":
            case "char":
            case "class":
            case "double":
            case "enum":
            case "extends":
            case "final":
            case "float":
            case "goto":
            case "implements":
            case "import":
            case "int":
            case "interface":
            case "long":
            case "native":
            case "package":
            case "private":
            case "protected":
            case "public":
            case "short":
            case "static":
            case "super":
            case "synchronized":
            case "throws":
            case "transient":
            case "volatile":
                return Token.RESERVED;
        }
        return Token.EOF;
    }

    /** ECMAScript 6. */
    private static Token stringToKeywordForES(final String name, final boolean isStrict) {
        switch (name) {
            case "break":
                return Token.BREAK;
            case "case":
                return Token.CASE;
            case "catch":
                return Token.CATCH;
            case "const":
                return Token.CONST;
            case "continue":
                return Token.CONTINUE;
            case "debugger":
                return Token.DEBUGGER;
            case "default":
                return Token.DEFAULT;
            case "delete":
                return Token.DELPROP;
            case "do":
                return Token.DO;
            case "else":
                return Token.ELSE;
            case "export":
                return Token.EXPORT;
            case "finally":
                return Token.FINALLY;
            case "for":
                return Token.FOR;
            case "function":
                return Token.FUNCTION;
            case "if":
                return Token.IF;
            case "import":
                return Token.IMPORT;
            case "in":
                return Token.IN;
            case "instanceof":
                return Token.INSTANCEOF;
            case "new":
                return Token.NEW;
            case "return":
                return Token.RETURN;
            case "switch":
                return Token.SWITCH;
            case "this":
                return Token.THIS;
            case "throw":
                return Token.THROW;
            case "try":
                return Token.TRY;
            case "typeof":
                return Token.TYPEOF;
            case "var":
                return Token.VAR;
            case "void":
                return Token.VOID;
            case "while":
                return Token.WHILE;
            case "with":
                return Token.WITH;
            case "yield":
                return Token.YIELD;
            case "false":
                return Token.FALSE;
            case "null":
                return Token.NULL;
            case "true":
                return Token.TRUE;
            case "let":
                return Token.LET;
            case "class":
            case "extends":
            case "super":
            case "await":
            case "enum":
                return Token.RESERVED;
            case "implements":
            case "interface":
            case "package":
            case "private":
            case "protected":
            case "public":
            case "static":
                if (isStrict) {
                    return Token.RESERVED;
                }
                break;
        }
        return Token.EOF;
    }

    @SuppressWarnings("checkstyle:MethodLength")
    final Token getToken() throws ParsingException {
        int c;

        for (;;) {
            // Eat whitespace, possibly sensitive to newlines.
            for (;;) {
                c = getChar();
                if (c == EOF_CHAR) {
                    tokenBeg = cursor - 1;
                    tokenEnd = cursor;
                    return Token.EOF;
                } else if (c == '\n') {
                    dirtyLine = false;
                    tokenBeg = cursor - 1;
                    tokenEnd = cursor;
                    return Token.EOL;
                } else if (!isJSSpace(c)) {
                    if (c != '-') {
                        dirtyLine = true;
                    }
                    break;
                }
            }

            // Assume the token will be 1 char - fixed up below.
            tokenBeg = cursor - 1;
            tokenEnd = cursor;

            // identifier/keyword/instanceof?
            // watch out for starting with a <backslash>
            final boolean identifierStart;
            boolean isUnicodeEscapeStart = false;
            if (c == '\\') {
                c = getChar();
                if (c == 'u') {
                    identifierStart = true;
                    isUnicodeEscapeStart = true;
                    stringBufferTop = 0;
                } else {
                    identifierStart = false;
                    ungetChar(c);
                    c = '\\';
                }
            } else {
                identifierStart = Character.isJavaIdentifierStart((char) c);
                if (identifierStart) {
                    stringBufferTop = 0;
                    addToString(c);
                }
            }

            if (identifierStart) {
                boolean containsEscape = isUnicodeEscapeStart;
                for (;;) {
                    if (isUnicodeEscapeStart) {
                        // strictly speaking we should probably push-back
                        // all the bad characters if the <backslash>uXXXX
                        // sequence is malformed. But since there isn't a
                        // correct context(is there?) for a bad Unicode
                        // escape sequence in an identifier, we can report
                        // an error here.
                        int escapeVal = 0;
                        for (int i = 0; i != 4; ++i) {
                            c = getChar();
                            escapeVal = Kit.xDigitToInt(c, escapeVal);
                            // Next check takes care about c < 0 and bad escape
                            if (escapeVal < 0) {
                                break;
                            }
                        }
                        if (escapeVal < 0) {
                            throw new ParsingException("invalid unicode escape");
                        }
                        addToString(escapeVal);
                        isUnicodeEscapeStart = false;
                    } else {
                        c = getChar();
                        if (c == '\\') {
                            c = getChar();
                            if (c == 'u') {
                                isUnicodeEscapeStart = true;
                                containsEscape = true;
                            } else {
                                throw new ParsingException(
                                        String.format("illegal character: '%c'", c));
                            }
                        } else {
                            if (c == EOF_CHAR
                                    || c == BYTE_ORDER_MARK
                                    || !Character.isJavaIdentifierPart((char) c)) {
                                break;
                            }
                            addToString(c);
                        }
                    }
                }
                ungetChar(c);

                String str = getStringFromBuffer();
                if (!containsEscape) {
                    // OPT we shouldn't have to make a string (object!) to
                    // check if it's a keyword.

                    // Return the corresponding token if it's a keyword
                    Token result = stringToKeyword(str, languageVersion, STRICT_MODE);
                    if (result != Token.EOF) {
                        if ((result == Token.LET || result == Token.YIELD)
                                && languageVersion < Context.VERSION_1_7) {
                            // LET and YIELD are tokens only in 1.7 and later
                            string = result == Token.LET ? "let" : "yield";
                            result = Token.NAME;
                        }
                        // Save the string in case we need to use in
                        // object literal definitions.
                        this.string = (String) allStrings.intern(str);
                        if (result != Token.RESERVED) {
                            return result;
                        } else if (languageVersion >= Context.VERSION_ES6) {
                            return result;
                        } else if (!IS_RESERVED_KEYWORD_AS_IDENTIFIER) {
                            return result;
                        }
                    }
                } else if (isKeyword(
                        str,
                        languageVersion,
                        STRICT_MODE)) {
                    // If a string contains unicodes, and converted to a keyword,
                    // we convert the last character back to unicode
                    str = convertLastCharToHex(str);
                }
                this.string = (String) allStrings.intern(str);
                return Token.NAME;
            }

            // is it a number?
            if (isDigit(c) || (c == '.' && isDigit(peekChar()))) {
                stringBufferTop = 0;
                int base = 10;
                final boolean es6 = languageVersion >= Context.VERSION_ES6;
                boolean isOldOctal = false;

                if (c == '0') {
                    c = getChar();
                    if (c == 'x' || c == 'X') {
                        base = 16;
                        c = getChar();
                    } else if (es6 && (c == 'o' || c == 'O')) {
                        base = 8;
                        c = getChar();
                    } else if (es6 && (c == 'b' || c == 'B')) {
                        base = 2;
                        c = getChar();
                    } else if (isDigit(c)) {
                        base = 8;
                        isOldOctal = true;
                    } else {
                        addToString('0');
                    }
                }

                final int emptyDetector = stringBufferTop;
                if (base == 10 || base == 16 || (base == 8 && !isOldOctal) || base == 2) {
                    c = readDigits(base, c);
                    if (c == REPORT_NUMBER_FORMAT_ERROR) {
                        throw new ParsingException("number format error");
                    }
                } else {
                    while (isDigit(c)) {
                        // finally the oldOctal case
                        if (c >= '8') {
                            /*
                             * We permit 08 and 09 as decimal numbers, which
                             * makes our behavior a superset of the ECMA
                             * numeric grammar.  We might not always be so
                             * permissive, so we warn about it.
                             */
                            base = 10;

                            c = readDigits(base, c);
                            if (c == REPORT_NUMBER_FORMAT_ERROR) {
                                throw new ParsingException("number format error");
                            }
                            break;
                        }
                        addToString(c);
                        c = getChar();
                    }
                }
                if (stringBufferTop == emptyDetector && base != 10) {
                    throw new ParsingException("number format error");
                }

                if (es6 && c == 'n') {
                    c = getChar();
                } else if (base == 10 && (c == '.' || c == 'e' || c == 'E')) {
                    if (c == '.') {
                        addToString(c);
                        c = getChar();
                        c = readDigits(base, c);
                        if (c == REPORT_NUMBER_FORMAT_ERROR) {
                            throw new ParsingException("number format error");
                        }
                    }
                    if (c == 'e' || c == 'E') {
                        addToString(c);
                        c = getChar();
                        if (c == '+' || c == '-') {
                            addToString(c);
                            c = getChar();
                        }
                        if (!isDigit(c)) {
                            throw new ParsingException("missing exponent");
                        }
                        c = readDigits(base, c);
                        if (c == REPORT_NUMBER_FORMAT_ERROR) {
                            throw new ParsingException("number format error");
                        }
                    }
                }
                ungetChar(c);
                this.string = getStringFromBuffer();
                return Token.NUMBER;
            }

            // is it a string or template literal?
            if (c == '"' || c == '\'' || c == '`') {
                // We attempt to accumulate a string the fast way, by
                // building it directly out of the reader.  But if there
                // are any escaped characters in the string, we revert to
                // building it out of a StringBuffer.

                // delimiter for last string literal scanned
                final int quoteChar = c;
                stringBufferTop = 0;

                c = getCharIgnoreLineEnd(false);
                strLoop:
                while (c != quoteChar) {
                    boolean unterminated = false;
                    if (c == EOF_CHAR) {
                        unterminated = true;
                    } else if (c == '\n') {
                        switch (lineEndChar) {
                            case '\n':
                            case '\r':
                                unterminated = true;
                                break;
                            case 0x2028: // <LS>
                            case 0x2029: // <PS>
                                // Line/Paragraph separators need to be included as is
                                c = lineEndChar;
                                break;
                            default:
                                break;
                        }
                    }

                    if (unterminated) {
                        throw new ParsingException("unterminated string literal");
                    }

                    if (c == '\\') {
                        // We've hit an escaped character
                        int escapeVal;

                        c = getChar();
                        switch (c) {
                            case 'b':
                                c = '\b';
                                break;
                            case 'f':
                                c = '\f';
                                break;
                            case 'n':
                                c = '\n';
                                break;
                            case 'r':
                                c = '\r';
                                break;
                            case 't':
                                c = '\t';
                                break;

                            // \v a late addition to the ECMA spec,
                            // it is not in Java, so use 0xb
                            case 'v':
                                c = 0xb;
                                break;

                            case 'u':
                                // Get 4 hex digits; if the u escape is not
                                // followed by 4 hex digits, use 'u' + the
                                // literal character sequence that follows.
                                final int escapeStart = stringBufferTop;
                                addToString('u');
                                escapeVal = 0;
                                for (int i = 0; i != 4; ++i) {
                                    c = getChar();
                                    escapeVal = Kit.xDigitToInt(c, escapeVal);
                                    if (escapeVal < 0) {
                                        continue strLoop;
                                    }
                                    addToString(c);
                                }
                                // prepare for replace of stored 'u' sequence
                                // by escape value
                                stringBufferTop = escapeStart;
                                c = escapeVal;
                                break;
                            case 'x':
                                // Get 2 hex digits, defaulting to 'x'+literal
                                // sequence, as above.
                                c = getChar();
                                escapeVal = Kit.xDigitToInt(c, 0);
                                if (escapeVal < 0) {
                                    addToString('x');
                                    continue strLoop;
                                }
                                final int c1 = c;
                                c = getChar();
                                escapeVal = Kit.xDigitToInt(c, escapeVal);
                                if (escapeVal < 0) {
                                    addToString('x');
                                    addToString(c1);
                                    continue strLoop;
                                }
                                // got 2 hex digits
                                c = escapeVal;
                                break;

                            case '\n':
                                // Remove line terminator after escape to follow
                                // SpiderMonkey and C/C++
                                c = getChar();
                                continue strLoop;

                            default:
                                if ('0' <= c && c < '8') {
                                    int val = c - '0';
                                    c = getChar();
                                    if ('0' <= c && c < '8') {
                                        val = 8 * val + c - '0';
                                        c = getChar();
                                        if ('0' <= c && c < '8' && val <= 037) {
                                            // c is 3rd char of octal sequence only
                                            // if the resulting val <= 0377
                                            val = 8 * val + c - '0';
                                            c = getChar();
                                        }
                                    }
                                    ungetChar(c);
                                    c = val;
                                }
                        }
                    }
                    addToString(c);
                    c = getChar(false);
                }

                final String str = getStringFromBuffer();
                this.string = (String) allStrings.intern(str);
                return quoteChar == '`' ? Token.TEMPLATE_LITERAL : Token.STRING;
            }

            switch (c) {
                case ';':
                    return Token.SEMI;
                case '[':
                    return Token.LB;
                case ']':
                    return Token.RB;
                case '{':
                    return Token.LC;
                case '}':
                    return Token.RC;
                case '(':
                    return Token.LP;
                case ')':
                    return Token.RP;
                case ',':
                    return Token.COMMA;
                case '?':
                    return Token.HOOK;
                case ':':
                    return Token.COLON;
                case '.':
                    return Token.DOT;

                case '|':
                    if (matchChar('|')) {
                        return Token.OR;
                    } else if (matchChar('=')) {
                        return Token.ASSIGN_BITOR;
                    } else {
                        return Token.BITOR;
                    }

                case '^':
                    if (matchChar('=')) {
                        return Token.ASSIGN_BITXOR;
                    }
                    return Token.BITXOR;

                case '&':
                    if (matchChar('&')) {
                        return Token.AND;
                    } else if (matchChar('=')) {
                        return Token.ASSIGN_BITAND;
                    } else {
                        return Token.BITAND;
                    }

                case '=':
                    if (matchChar('=')) {
                        if (matchChar('=')) {
                            return Token.SHEQ;
                        }
                        return Token.EQ;
                    } else if (matchChar('>')) {
                        return Token.ARROW;
                    } else {
                        return Token.ASSIGN;
                    }

                case '!':
                    if (matchChar('=')) {
                        if (matchChar('=')) {
                            return Token.SHNE;
                        }
                        return Token.NE;
                    }
                    return Token.NOT;

                case '<':
                    /* NB:treat HTML begin-comment as comment-till-eol */
                    if (matchChar('!')) {
                        if (matchChar('-')) {
                            if (matchChar('-')) {
                                tokenBeg = cursor - 4;
                                skipLine();
                                return Token.COMMENT;
                            }
                            ungetCharIgnoreLineEnd('-');
                        }
                        ungetCharIgnoreLineEnd('!');
                    }
                    if (matchChar('<')) {
                        if (matchChar('=')) {
                            return Token.ASSIGN_LSH;
                        }
                        return Token.LSH;
                    }
                    if (matchChar('=')) {
                        return Token.LE;
                    }
                    return Token.LT;

                case '>':
                    if (matchChar('>')) {
                        if (matchChar('>')) {
                            if (matchChar('=')) {
                                return Token.ASSIGN_URSH;
                            }
                            return Token.URSH;
                        }
                        if (matchChar('=')) {
                            return Token.ASSIGN_RSH;
                        }
                        return Token.RSH;
                    }
                    if (matchChar('=')) {
                        return Token.GE;
                    }
                    return Token.GT;

                case '*':
                    if (languageVersion >= Context.VERSION_ES6) {
                        if (matchChar('*')) {
                            if (matchChar('=')) {
                                return Token.ASSIGN_EXP;
                            }
                            return Token.EXP;
                        }
                    }
                    if (matchChar('=')) {
                        return Token.ASSIGN_MUL;
                    }
                    return Token.MUL;

                case '/':
                    // is it a // comment?
                    if (matchChar('/')) {
                        tokenBeg = cursor - 2;
                        skipLine();
                        return Token.COMMENT;
                    }
                    // is it a /* or /** comment?
                    if (matchChar('*')) {
                        boolean lookForSlash = false;
                        tokenBeg = cursor - 2;
                        if (matchChar('*')) {
                            lookForSlash = true;
                        }
                        for (;;) {
                            c = getChar();
                            if (c == EOF_CHAR) {
                                tokenEnd = cursor - 1;
                                throw new ParsingException("unterminated comment");
                            } else if (c == '*') {
                                lookForSlash = true;
                            } else if (c == '/') {
                                if (lookForSlash) {
                                    tokenEnd = cursor;
                                    return Token.COMMENT;
                                }
                            } else {
                                lookForSlash = false;
                                tokenEnd = cursor;
                            }
                        }
                    }

                    if (matchChar('=')) {
                        return Token.ASSIGN_DIV;
                    }
                    return Token.DIV;

                case '%':
                    if (matchChar('=')) {
                        return Token.ASSIGN_MOD;
                    }
                    return Token.MOD;

                case '~':
                    return Token.BITNOT;

                case '+':
                    if (matchChar('=')) {
                        return Token.ASSIGN_ADD;
                    } else if (matchChar('+')) {
                        return Token.INC;
                    } else {
                        return Token.ADD;
                    }

                case '-':
                    Token t = Token.SUB;
                    if (matchChar('=')) {
                        t = Token.ASSIGN_SUB;
                    } else if (matchChar('-')) {
                        if (!dirtyLine) {
                            // treat HTML end-comment after possible whitespace
                            // after line start as comment-until-eol
                            if (matchChar('>')) {
                                skipLine();
                                return Token.COMMENT;
                            }
                        }
                        t = Token.DEC;
                    }
                    dirtyLine = true;
                    return t;

                default:
                    throw new ParsingException(String.format("illegal character: '%c'", c));
            }
        }
    }

    /*
     * Helper to read the next digits according to the base
     * and ignore the number separator if there is one.
     */
    private int readDigits(final int base, final int firstC) {
        if (isDigit(base, firstC)) {
            addToString(firstC);

            int c = getChar();
            if (c == EOF_CHAR) {
                return EOF_CHAR;
            }

            while (true) {
                if (c == NUMERIC_SEPARATOR) {
                    // we do no peek here, we are optimistic for performance
                    // reasons and because peekChar() only does an getChar/ungetChar.
                    c = getChar();
                    // if the line ends after the separator we have
                    // to report this as an error
                    if (c == '\n' || c == EOF_CHAR) {
                        return REPORT_NUMBER_FORMAT_ERROR;
                    }

                    if (!isDigit(base, c)) {
                        // bad luck we have to roll back
                        ungetChar(c);
                        return NUMERIC_SEPARATOR;
                    }
                    addToString(NUMERIC_SEPARATOR);
                } else if (isDigit(base, c)) {
                    addToString(c);
                    c = getChar();
                    if (c == EOF_CHAR) {
                        return EOF_CHAR;
                    }
                } else {
                    return c;
                }
            }
        }
        return firstC;
    }

    private static boolean isAlpha(final int c) {
        // Use 'Z' < 'a'
        if (c <= 'Z') {
            return 'A' <= c;
        }
        return 'a' <= c && c <= 'z';
    }

    private static boolean isDigit(final int base, final int c) {
        return (base == 10 && isDigit(c))
                || (base == 16 && isHexDigit(c))
                || (base == 8 && isOctalDigit(c))
                || (base == 2 && isDualDigit(c));
    }

    private static boolean isDualDigit(final int c) {
        return '0' == c || c == '1';
    }

    private static boolean isOctalDigit(final int c) {
        return '0' <= c && c <= '7';
    }

    private static boolean isDigit(final int c) {
        return '0' <= c && c <= '9';
    }

    private static boolean isHexDigit(final int c) {
        return ('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F');
    }

    /* As defined in ECMA.  jsscan.c uses C isspace() (which allows
     * \v, I think.)  note that code in getChar() implicitly accepts
     * '\r' == \u000D as well.
     */
    private static boolean isJSSpace(final int c) {
        if (c <= 127) {
            return c == 0x20 || c == 0x9 || c == 0xC || c == 0xB;
        }
        return c == 0xA0
                || c == BYTE_ORDER_MARK
                || Character.getType((char) c) == Character.SPACE_SEPARATOR;
    }

    private static boolean isJSFormatChar(final int c) {
        return c > 127 && Character.getType((char) c) == Character.FORMAT;
    }

    /** Parser calls the method when it gets / or /= in literal context. */
    void readRegExp(final Token startToken) throws ParsingException {
        final int start = tokenBeg;
        stringBufferTop = 0;
        if (startToken == Token.ASSIGN_DIV) {
            // Miss-scanned /=
            addToString('=');
        } else {
            if (startToken != Token.DIV) {
                Kit.codeBug();
            }
            if (peekChar() == '*') {
                tokenEnd = cursor - 1;
                this.string = new String(stringBuffer, 0, stringBufferTop);
                throw new ParsingException("msg.unterminated.re.lit");
            }
        }

        boolean inCharSet = false; // true if inside a '['..']' pair
        int c;
        while ((c = getChar()) != '/' || inCharSet) {
            if (c == '\n' || c == EOF_CHAR) {
                throw new ParsingException("msg.unterminated.re.lit");
            }
            if (c == '\\') {
                addToString(c);
                c = getChar();
                if (c == '\n' || c == EOF_CHAR) {
                    throw new ParsingException("msg.unterminated.re.lit");
                }
            } else if (c == '[') {
                inCharSet = true;
            } else if (c == ']') {
                inCharSet = false;
            }
            addToString(c);
        }
        final int reEnd = stringBufferTop;

        while (true) {
            c = getCharIgnoreLineEnd();
            if ("gimysu".indexOf(c) != -1) {
                addToString(c);
            } else if (isAlpha(c)) {
                throw new ParsingException("msg.invalid.re.flag");
            } else {
                ungetCharIgnoreLineEnd(c);
                break;
            }
        }

        tokenEnd = start + stringBufferTop + 2; // include slashes
        this.string = new String(stringBuffer, 0, reEnd);
    }

    private String getStringFromBuffer() {
        tokenEnd = cursor;
        return new String(stringBuffer, 0, stringBufferTop);
    }

    private void addToString(final int c) {
        final int n = stringBufferTop;
        if (n == stringBuffer.length) {
            final char[] tmp = new char[stringBuffer.length * 2];
            System.arraycopy(stringBuffer, 0, tmp, 0, n);
            stringBuffer = tmp;
        }
        stringBuffer[n] = (char) c;
        stringBufferTop = n + 1;
    }

    private void ungetChar(final int c) {
        // can not unread past across line boundary
        if (ungetCursor != 0 && ungetBuffer[ungetCursor - 1] == '\n') {
            Kit.codeBug();
        }
        ungetBuffer[ungetCursor++] = c;
        cursor--;
    }

    private boolean matchChar(final int test) {
        final int c = getCharIgnoreLineEnd();
        if (c == test) {
            tokenEnd = cursor;
            return true;
        }
        ungetCharIgnoreLineEnd(c);
        return false;
    }

    private int peekChar() {
        final int c = getChar();
        ungetChar(c);
        return c;
    }

    private int getChar() {
        return getChar(true, false);
    }

    private int getChar(final boolean skipFormattingChars) {
        return getChar(skipFormattingChars, false);
    }

    private int getChar(final boolean skipFormattingChars, final boolean ignoreLineEnd) {
        if (ungetCursor != 0) {
            cursor++;
            return ungetBuffer[--ungetCursor];
        }

        for (;;) {
            if (sourceCursor == sourceString.length()) {
                hitEOF = true;
                return EOF_CHAR;
            }
            cursor++;
            int c = sourceString.charAt(sourceCursor++);

            if (!ignoreLineEnd && lineEndChar >= 0) {
                if (lineEndChar == '\r' && c == '\n') {
                    lineEndChar = '\n';
                    continue;
                }
                lineEndChar = -1;
                lineStart = sourceCursor - 1;
                lineno++;
            }

            if (c <= 127) {
                if (c == '\n' || c == '\r') {
                    lineEndChar = c;
                    c = '\n';
                }
            } else {
                if (c == BYTE_ORDER_MARK) {
                    return c; // BOM is considered whitespace
                }
                if (skipFormattingChars && isJSFormatChar(c)) {
                    continue;
                }
                if (ScriptRuntime.isJSLineTerminator(c)) {
                    lineEndChar = c;
                    c = '\n';
                }
            }
            return c;
        }
    }

    private int getCharIgnoreLineEnd() {
        return getChar(true, true);
    }

    private int getCharIgnoreLineEnd(final boolean skipFormattingChars) {
        return getChar(skipFormattingChars, true);
    }

    private void ungetCharIgnoreLineEnd(final int c) {
        ungetBuffer[ungetCursor++] = c;
        cursor--;
    }

    @SuppressWarnings("checkstyle:emptyblock")
    private void skipLine() {
        // skip to end of line
        int c;
        while ((c = getChar()) != EOF_CHAR && c != '\n') { }
        ungetChar(c);
        tokenEnd = cursor;
    }

    /** Return the current position of the scanner cursor. */
    public int getCursor() {
        return cursor;
    }

    /** Return the absolute source offset of the last scanned token. */
    public int getTokenBeg() {
        return tokenBeg;
    }

    /** Return the absolute source end-offset of the last scanned token. */
    public int getTokenEnd() {
        return tokenEnd;
    }

    /** Return tokenEnd - tokenBeg */
    public int getTokenLength() {
        return tokenEnd - tokenBeg;
    }

    public String getTokenRaw() {
        return sourceString.substring(tokenBeg, tokenEnd);
    }

    private static String convertLastCharToHex(final String str) {
        final int lastIndex = str.length() - 1;
        final StringBuilder buf = new StringBuilder(str.substring(0, lastIndex));
        buf.append("\\u");
        final String hexCode = Integer.toHexString(str.charAt(lastIndex));
        for (int i = 0; i < 4 - hexCode.length(); ++i) {
            buf.append('0');
        }
        buf.append(hexCode);
        return buf.toString();
    }

    public Token nextToken() throws ParsingException {
        Token tt = getToken();
        while (tt == Token.EOL || tt == Token.COMMENT) {
            tt = getToken();
        }
        return tt;
    }

    // stuff other than whitespace since start of line
    private boolean dirtyLine;
    private String string = "";

    private char[] stringBuffer = new char[128];
    private int stringBufferTop;
    private final ObjToIntMap allStrings = new ObjToIntMap(50);

    // Room to backtrace from to < on failed match of the last - in <!--
    private final int[] ungetBuffer = new int[3];
    private int ungetCursor;

    private boolean hitEOF = false;

    private int lineStart = 0;
    private int lineEndChar = -1;
    int lineno;

    private final String sourceString;

    // sourceCursor is an index into a small buffer that keeps a
    // sliding window of the source stream.
    int sourceCursor;

    // cursor is a monotonically increasing index into the original
    // source stream, tracking exactly how far scanning has progressed.
    // Its value is the index of the next character to be scanned.
    int cursor;

    // Record start and end positions of last scanned token.
    int tokenBeg;
    int tokenEnd;

    private final int languageVersion;
    private static final boolean IS_RESERVED_KEYWORD_AS_IDENTIFIER = true;
    private static final boolean STRICT_MODE = false;
}
