parser grammar ManifestParser;

options {
    tokenVocab = ManifestLexer;
}

manifest
    :   mainSection entrySection*
    ;

mainSection
    :   ManifestVersion ValueSeparator Value
        attribute*
    ;

entrySection
    :   SectionName ValueSeparator Value
        attribute*
    ;

attribute
    :   AttributeName ValueSeparator Value
    ;
