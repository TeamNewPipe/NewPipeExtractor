package org.schabi.newpipe.extractor.utils.jsextractor;

public enum Token {
    ERROR,
    EOF,
    EOL,
    RETURN(false, false, true),
    BITOR(true, true, false),
    BITXOR(true, true, false),
    BITAND(true, true, false),
    EQ(true, true, false),
    NE(true, true, false),
    LT(true, true, false),
    LE(true, true, false),
    GT(true, true, false),
    GE(true, true, false),
    LSH(true, true, false),
    RSH(true, true, false),
    URSH(true, true, false),
    ADD(true, true, false),
    SUB(true, true, false),
    MUL(true, true, false),
    DIV(true, true, false),
    MOD(true, true, false),
    NOT(true, true, false),
    BITNOT(true, true, false),
    NEW(true, false, true),
    DELPROP(true, false, true),
    TYPEOF(true, false, true),
    NAME,
    NUMBER,
    STRING,
    NULL(false, false, true),
    THIS(false, false, true),
    FALSE(false, false, true),
    TRUE(false, false, true),
    SHEQ(true, true, false), // shallow equality (===)
    SHNE(true, true, false), // shallow inequality (!==)
    REGEXP,
    THROW(true, false, true),
    IN(true, false, true),
    INSTANCEOF(true, false, true),
    YIELD(false, false, true), // JS 1.7 yield pseudo keyword
    EXP(true, true, false), // Exponentiation Operator
    BIGINT, // ES2020 BigInt
    TRY(false, false, true),
    SEMI(false, true, false), // semicolon
    LB(false, true, false), // left and right brackets
    RB(false, true, false),
    LC(false, true, false), // left and right curlies (braces)
    RC(false, true, false),
    LP(false, true, false), // left and right parentheses
    RP(false, true, false),
    COMMA(false, true, false), // comma operator
    ASSIGN(true, true, false), // simple assignment  (=)
    ASSIGN_BITOR(true, true, false), // |=
    ASSIGN_BITXOR(true, true, false), // ^=
    ASSIGN_BITAND(true, true, false), // |=
    ASSIGN_LSH(true, true, false), // <<=
    ASSIGN_RSH(true, true, false), // >>=
    ASSIGN_URSH(true, true, false), // >>>=
    ASSIGN_ADD(true, true, false), // +=
    ASSIGN_SUB(true, true, false), // -=
    ASSIGN_MUL(true, true, false), // *=
    ASSIGN_DIV(true, true, false), // /=
    ASSIGN_MOD(true, true, false), // %=
    ASSIGN_EXP(true, true, false), // **=
    HOOK(true, true, false), // conditional (?:)
    COLON(true, true, false),
    OR(true, true, false), // logical or (||)
    AND(true, true, false), // logical and (&&)
    INC(true, true, false), // increment/decrement (++ --)
    DEC(true, true, false),
    DOT(false, true, false), // member operator (.)
    FUNCTION(false, false, true), // function keyword
    EXPORT(false, false, true), // export keyword
    IMPORT(false, false, true), // import keyword
    IF(false, false, true), // if keyword
    ELSE(false, false, true), // else keyword
    SWITCH(false, false, true), // switch keyword
    CASE(false, false, true), // case keyword
    DEFAULT(false, false, true), // default keyword
    WHILE(false, false, true), // while keyword
    DO(false, false, true), // do keyword
    FOR(false, false, true), // for keyword
    BREAK(false, false, true), // break keyword
    CONTINUE(false, false, true), // continue keyword
    VAR(false, false, true), // var keyword
    WITH(false, false, true), // with keyword
    CATCH(false, false, true), // catch keyword
    FINALLY(false, false, true), // finally keyword
    VOID(true, false, true), // void keyword
    RESERVED(false, false, true), // reserved keywords
    LET(false, false, true), // JS 1.7 let pseudo keyword
    CONST(false, false, true),
    DEBUGGER(false, false, true),
    COMMENT,
    ARROW(false, true, false), // ES6 ArrowFunction
    YIELD_STAR(false, false, true), // ES6 "yield *", a specialization of yield
    TEMPLATE_LITERAL; // template literal

    public final boolean isOp;
    public final boolean isPunct;
    public final boolean isKeyw;

    Token(final boolean isOp, final boolean isPunct, final boolean isKeyw) {
        this.isOp = isOp;
        this.isPunct = isPunct;
        this.isKeyw = isKeyw;
    }

    Token() {
        this.isOp = false;
        this.isPunct = false;
        this.isKeyw = false;
    }

    public boolean isConditional() {
        return this == IF || this == FOR || this == WHILE || this == WITH;
    }
}
