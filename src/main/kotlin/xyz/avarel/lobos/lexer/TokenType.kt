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

    RANGE_IN,
    RANGE_EX,

    PIPE,
    AND,
    OR,

    PLUS,
    MINUS,
    ASTERISK,
    F_SLASH,
    B_SLASH,

    ASSIGN,
    QUESTION,
    BANG,

    STRING,
    IDENT,
    INT,

    TRUE,
    FALSE,

    DOT,
    COMMA,
    COLON,
    SEMICOLON,

    LET,
    MUT,
    IF,

    INVALID,
}