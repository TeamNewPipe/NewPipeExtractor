package org.schabi.newpipe.extractor.utils.jsextractor;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class JavaScriptExtractor
{

    private static HashSet<Integer> operators = new HashSet<>(Arrays.asList(
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

    private static HashSet<Integer> punctuators = new HashSet<>(Arrays.asList(
            Token.LP, Token.RP, Token.LB, Token.RB, Token.LC, Token.RC,
            Token.COLON, Token.DOT, Token.DOTDOT, Token.DOTQUERY,
            Token.ARROW
    ));

    private static HashSet<Integer> keywords = new HashSet<>(Arrays.asList(
            Token.INSTANCEOF, Token.IN, Token.DELPROP, Token.TYPEOF, Token.VOID,
            Token.BREAK, Token.CASE, Token.CATCH, Token.RESERVED, Token.CONTINUE,
            Token.DEBUGGER, Token.DEFAULT, Token.DO, Token.ELSE, Token.EXPORT,
            Token.FINALLY, Token.FOR, Token.FUNCTION, Token.IF, Token.IMPORT,
            Token.LET, Token.NEW, Token.RETURN, Token.SWITCH, Token.THROW,
            Token.TRY, Token.VAR, Token.WHILE, Token.WITH, Token.NULL,
            Token.TRUE, Token.FALSE, Token.YIELD, Token.CONST

            // Token.THIS
    ));

    @Nonnull
    public static String extractFunction(final String playerJsCode,
                                         final String start) throws ParsingException, IOException {
        int startIndex = playerJsCode.indexOf(start);
        if (startIndex < 0) {
            throw new ParsingException("start not found");
        }
        startIndex += start.length();

        TokenStream ts = new TokenStream(null, playerJsCode.substring(startIndex), 0);
        ArrayList<Integer> tokenHistory = new ArrayList<>();
        int level = 0;
        int endIndex = startIndex;

        java.util.function.IntFunction<Integer> ltok = (n) -> tokenHistory.get(tokenHistory.size() - 1 - n);

        while(true) {
            int t = ts.nextToken();
            tokenHistory.add(t);
            endIndex = ts.getTokenEnd();

            // Regex check
            boolean isRegex = false;
            if (t == Token.DIV || t == Token.ASSIGN_DIV) {
                int lastTok = ltok.apply(1);
                int last2Tok = ltok.apply(2);

                java.util.function.BooleanSupplier isBlock = () -> {
                    if (last2Tok == Token.LP || last2Tok == Token.LB) {
                        return false;
                    }
                    // todo: record parent token
                    // else if (lastTok == Token.COLON && ) {}
                    else if (operators.contains(last2Tok)) {
                        return false;
                    }
                    else if (last2Tok == Token.RETURN || last2Tok == Token.YIELD || last2Tok == Token.YIELD_STAR) {
                        return false;
                        // Todo: if lineNumber(tok) isnt lineNumber(tok-1) return true
                    }
                    else if (last2Tok == Token.CASE) {
                        return false;
                    }
                    else return true;
                };

                if (lastTok == Token.LP || lastTok == Token.RP) {
                    if (last2Tok == Token.IF || last2Tok == Token.WHILE || last2Tok == Token.FOR || last2Tok == Token.WITH) {
                        isRegex = true;
                    }
                }
                else if (lastTok == Token.LC || lastTok == Token.RC) {
                    if (isBlock.getAsBoolean()) {
                        // named or anonymous function
                        if ((last2Tok == Token.LP || last2Tok == Token.RP) &&
                            (ltok.apply(3) == Token.FUNCTION || ltok.apply(4) == Token.FUNCTION)) {
                            // Todo: check function expression
                        } else {
                            isRegex = true;
                        }
                    }
                }
                else if (punctuators.contains(lastTok) || operators.contains(lastTok) || keywords.contains(lastTok)) {
                    isRegex = true;
                }
            }

            if (isRegex) {
                ts.readRegExp(t);
            }

            if (t == Token.LC) level++;
            else if (t == Token.RC) {
                level--;
                if (level == 0) break;
            }
            else if(t == Token.EOF) break;
        }

        if (level != 0) {
            throw new ParsingException("could not find matching braces");
        }

        return playerJsCode.substring(startIndex, endIndex + startIndex);
    }
}
