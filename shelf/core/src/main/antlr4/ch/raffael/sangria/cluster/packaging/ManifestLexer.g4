lexer grammar ManifestLexer;

SectionName
    :   [nN][aA][mM][eE]
    ;

ManifestVersion
    :   [mM][aA][nN][iI][fF][eE][sS][tT]'-'[vV][eE][rR][sS][iI][oO][nN]
    ;

AttributeName
    :   AlphaNum (AlphaNum|'-'|'_')*
    ;

ValueSeparator
    :   ':' Space*
    ->  pushMode(ValueMode)
    ;

fragment AlphaNum
    :   'a'..'z'|'A'..'Z'|'0'..'9'
    ;

fragment Space
    : [ \t]+
    ;

fragment NewLine
    : '\n' | '\r' '\n'?
    ;

mode ValueMode;

Value
    :   ValueChar* (NewLine Space+ ValueChar*)* NewLine+
    ->  popMode
    ;

fragment ValueChar
    :   ~('\n'|'\r')
    ;
