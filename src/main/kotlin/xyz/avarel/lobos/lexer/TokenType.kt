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

    TRUE,
    FALSE,

    DOT,
    COMMA,
    COLON,
    SEMICOLON,

    RETURN,
    LET,
    MUT,
    IF,
    ELSE,
    NULL,
    TYPE,

    INVALID,
}