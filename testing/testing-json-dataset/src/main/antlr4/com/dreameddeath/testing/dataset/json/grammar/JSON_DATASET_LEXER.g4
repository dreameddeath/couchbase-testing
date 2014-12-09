lexer grammar JSON_DATASET_LEXER;



MVEL_MULTILINE : '#%' -> pushMode(MvelMultilineMode);
MVEL_MONOLINE_START : '#%%' ->pushMode(MvelMonolineMode);

BASENAME : [a-zA-Z][a-zA-Z0-9_-]* ;

STRING : '"' (ESC | ~["\\])* '"' ;
fragment ESC : '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;
DECIMAL
: '-'? INT '.' [0-9]+ EXP?; // 1.35, 1.35E-9, 0.3, -4.5

EXP_INT : '-'? INT EXP // 1e10 -3e4
;
fragment INT : '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP : [Ee] [+\-]? INT ; // \- since - means "range" inside [...]
WS : [ \t\n\r]+ -> skip ;


INTEGER :
    '-'? INT;

TRUE: 'true';
FALSE : 'false';
NULL : 'null';
PATH_ANY_RECURSIVE : '**';
PATH_ANY : '*';
ARRAY_START:'[';
ARRAY_END:']';
OBJECT_START:'{';
OBJECT_END:'}';
RANGE_SEP : '..';
DOT : '.';
FIELD_SEPARATOR : ',';
FIELD_VAL_SEP : ':';

mode MvelMultilineMode;

MVEL_MULTILINE_COMMENT_COMPLEX : '/*' .*? '*/' ;
MVEL_MULTILINE_COMMENT_SIMPLE : '//' [^\r\n]*? [\r]? [\n];

MVEL_MULTILINE_DQ_STRING : '"' (MVEL_MULTILINE_DQ_STRING_ESC | ~["\\])* '"' ;
fragment MVEL_MULTILINE_DQ_STRING_ESC : '\\' (["\\/bfnrt] | MVEL_MULTILINE_DQ_STRING_UNICODE) ;
fragment MVEL_MULTILINE_DQ_STRING_UNICODE : 'u' MVEL_HEX MVEL_HEX MVEL_HEX MVEL_HEX;
fragment MVEL_HEX : [0-9a-fA-F];
MVEL_MULTILINE_SQ_STRING : '\'' (MVEL_MULTILINE_SQ_STRING_ESC | ~['\\])* '\'' ;
fragment MVEL_MULTILINE_SQ_STRING_ESC : '\\' (['\\/bfnrt] | MVEL_MULTILINE_SQ_STRING_UNICODE) ;
fragment MVEL_MULTILINE_SQ_STRING_UNICODE : 'u' MVEL_HEX MVEL_HEX MVEL_HEX MVEL_HEX ;

MVEL_MULTILINE_END : '%#' -> popMode;
MVEL_MULTILINE_CONTENT : . ;


mode MvelMonolineMode;
MVEL_MONOLINE_CONTENT :
    [^\n\r]* [\r]? [\n] ->popMode
    ;

