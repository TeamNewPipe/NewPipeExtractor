package org.schabi.newpipe.extractor.utils.jsextractor;

/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * This class implements the JavaScript scanner.
 *
 * <p>It is based on the C source files jsscan.c and jsscan.h in the jsref package.
 *
 * @see org.mozilla.javascript.Parser
 * @author Mike McCabe
 * @author Brendan Eich
 */
public class Token {

    /** Token types. These values correspond to JSTokenType values in jsscan.c. */
    public static final int
            // start enum
            ERROR = -1, // well-known as the only code < EOF
            EOF = 0, // end of file token - (not EOF_CHAR)
            EOL = 1, // end of line

            RETURN = 4,
            BITOR = 9,
            BITXOR = 10,
            BITAND = 11,
            EQ = 12,
            NE = 13,
            LT = 14,
            LE = 15,
            GT = 16,
            GE = 17,
            LSH = 18,
            RSH = 19,
            URSH = 20,
            ADD = 21,
            SUB = 22,
            MUL = 23,
            DIV = 24,
            MOD = 25,
            NOT = 26,
            BITNOT = 27,
            NEW = 30,
            DELPROP = 31,
            TYPEOF = 32,
            NAME = 39,
            NUMBER = 40,
            STRING = 41,
            NULL = 42,
            THIS = 43,
            FALSE = 44,
            TRUE = 45,
            SHEQ = 46, // shallow equality (===)
            SHNE = 47, // shallow inequality (!==)
            THROW = 50,
            IN = 52,
            INSTANCEOF = 53,
            YIELD = 73, // JS 1.7 yield pseudo keyword
            EXP = 75, // Exponentiation Operator

            BIGINT = 83; // ES2020 BigInt

    // End of interpreter bytecodes
    public static final int LAST_BYTECODE_TOKEN = BIGINT,
            TRY = 84,
            SEMI = 85, // semicolon
            LB = 86, // left and right brackets
            RB = 87,
            LC = 88, // left and right curlies (braces)
            RC = 89,
            LP = 90, // left and right parentheses
            RP = 91,
            COMMA = 92, // comma operator
            ASSIGN = 93, // simple assignment  (=)
            ASSIGN_BITOR = 94, // |=
            ASSIGN_BITXOR = 95, // ^=
            ASSIGN_BITAND = 96, // |=
            ASSIGN_LSH = 97, // <<=
            ASSIGN_RSH = 98, // >>=
            ASSIGN_URSH = 99, // >>>=
            ASSIGN_ADD = 100, // +=
            ASSIGN_SUB = 101, // -=
            ASSIGN_MUL = 102, // *=
            ASSIGN_DIV = 103, // /=
            ASSIGN_MOD = 104, // %=
            ASSIGN_EXP = 105; // **=
    public static final int
            HOOK = 106, // conditional (?:)
            COLON = 107,
            OR = 108, // logical or (||)
            AND = 109, // logical and (&&)
            INC = 110, // increment/decrement (++ --)
            DEC = 111,
            DOT = 112, // member operator (.)
            FUNCTION = 113, // function keyword
            EXPORT = 114, // export keyword
            IMPORT = 115, // import keyword
            IF = 116, // if keyword
            ELSE = 117, // else keyword
            SWITCH = 118, // switch keyword
            CASE = 119, // case keyword
            DEFAULT = 120, // default keyword
            WHILE = 121, // while keyword
            DO = 122, // do keyword
            FOR = 123, // for keyword
            BREAK = 124, // break keyword
            CONTINUE = 125, // continue keyword
            VAR = 126, // var keyword
            WITH = 127, // with keyword
            CATCH = 128, // catch keyword
            FINALLY = 129, // finally keyword
            VOID = 130, // void keyword
            RESERVED = 131, // reserved keywords


            LET = 157, // JS 1.7 let pseudo keyword
            CONST = 158,
            DEBUGGER = 164,
            COMMENT = 165,
            ARROW = 168, // ES6 ArrowFunction
            YIELD_STAR = 169, // ES6 "yield *", a specialization of yield
            TEMPLATE_LITERAL = 170; // template literal
}
