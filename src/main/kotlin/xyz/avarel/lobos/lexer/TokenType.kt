package xyz.avarel.lobos.lexer

enum class TokenType {
    L_BRACE,
    R_BRACE,

    L_PAREN,
    R_PAREN,

    L_BRACKET,
    R_BRACKET,

    LT,
    GT,
    LTE,
    GTE,

    EQ,
    NEQ,

    RANGE_IN,
    RANGE_EX,

    AMP,
    PIPE,
    AND,
    OR,

    PIPE_FORWARD,

    PLUS,
    MINUS,
    ASTERISK,
    F_SLASH,
    B_SLASH,

    ASSIGN,
    QUESTION,
    BANG,
    ARROW,

    STRING,
    IDENT,

    INT,
    DECIMAL,

    TRUE,
    FALSE,

    DOT,
    COMMA,
    COLON,
    SEMICOLON,

    NL,

    AS,
    IS,
    EXTERNAL,
    MUT,
    RETURN,
    LET,
    DEF,
    IF,
    ELSE,
    NULL,
    TYPE,

    INVALID,
    RESERVED
}