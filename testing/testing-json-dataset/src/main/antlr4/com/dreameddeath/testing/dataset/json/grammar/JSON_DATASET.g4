/** Taken from "The Definitive ANTLR 4 Reference" by Terence Parr */
// Derived from http://json.org
parser grammar JSON_DATASET;

@header{
  import java.util.Map;
  import java.util.HashMap;
  import java.util.Collections;
  import java.util.List;
  import java.util.ArrayList;
}

options {   tokenVocab = JSON_DATASET_LEXER; }


dataset :
    monoline_rule |
    multiline_rule |
    json
    ;

monoline_rule :
    MVEL_MONOLINE_START MVEL_MONOLINE_CONTENT
    ;

multiline_rule :
    MVEL_MULTILINE
    (
        MVEL_MULTILINE_COMMENT_COMPLEX |
        MVEL_MULTILINE_COMMENT_SIMPLE |
        MVEL_MULTILINE_DQ_STRING |
        MVEL_MULTILINE_SQ_STRING |
        MVEL_MULTILINE_CONTENT
     ) *
    MVEL_MULTILINE_END
    ;



json returns [ Object result ] : object { $result = $object.result;}
| array {$result = $array.result;}
;

object returns [ Map<Object,Object> result ]
       @init{ $result = new HashMap<Object,Object>();}
: OBJECT_START pair (FIELD_SEPARATOR pair)* OBJECT_END //{ $result = $attributes;}
| OBJECT_START OBJECT_END //{$result = Collections.emptyMap(); }// empty object
;

pair :
    json_path FIELD_VAL_SEP value {$object::result.put($json_path.result,$value.result);}
    ;

json_path
    returns [ List<Object> result]
    @init{$result = new ArrayList<Object>();}
 :
    path_part {$result.add($path_part.text);} ( DOT path_part {$result.add($path_part.text);} )* //{ $result.add($path_part.text); $result.addAll($json_path.result);}
    ;

path_part :
    basename ARRAY_START offsets ARRAY_END |
    basename
    //basename '(' predicate ')' |
    //basename '[' offsets ']' '(' predicate ')'
    ;

basename:
    STRING |
    BASENAME |
    PATH_ANY |
    PATH_ANY_RECURSIVE
;

offsets :
    INTEGER |
    INTEGER RANGE_SEP INTEGER
    ;

array
    returns [ List<Object> result ]
    @init{$result= new ArrayList();}
: ARRAY_START value {$result.add($value.result);} (FIELD_SEPARATOR value {$result.add($value.result);} )* ARRAY_END//{ $result = $elements;}
| ARRAY_START ARRAY_END //{ $result = Collections.emptyList();}// empty array
;

value returns [ Object result ]
: STRING { $result = $STRING.text;System.out.println($STRING.text);}
| DECIMAL { $result = Double.parseDouble($DECIMAL.text); }
| INTEGER { $result = Long.parseLong($INTEGER.text);}
| EXP_INT { $result = Long.parseLong($EXP_INT.text);}
| object { $result = $object.result;}// recursion
| array { $result = $array.result;}// recursion
| TRUE { $result = new Boolean(true);}// keywords
| FALSE { $result = new Boolean(false);}
| NULL { $result = null;}
;
