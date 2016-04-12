/** Taken from "The Definitive ANTLR 4 Reference" by Terence Parr */
// Derived from http://json.org
parser grammar JSON_DATASET;

@header{
  import java.util.Map;
  import java.util.HashMap;
  import java.util.Collections;
  import java.util.List;
  import java.util.ArrayList;
  import com.dreameddeath.testing.dataset.json.*;
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
    returns [ JsonXPath result ]
    @init{$result = new JsonXPath();}
 :
    (meta_data {$result.addMeta($meta_data.result);})* path_part {$result.addPart($path_part.result);} ( DOT path_part {$result.addPart($path_part.result);} )* //{ $result.add($path_part.text); $result.addAll($json_path.result);}
    ;


meta_data returns [ JsonMeta result ] @init{$result = new JsonMeta();}:
    META_CHAR basename {$result.setName($basename.text);} |
    META_CHAR basename {$result.setName($basename.text);} PARENTHESIS_START PARENTHESIS_END;



path_part returns [ JsonXPathPart result ] @init{$result=new JsonXPathPart();}:
    basename {$result.setLocalName($basename.text);} ARRAY_START offsets {$result.setOffset($offsets.result);} ARRAY_END |
    basename {$result.setLocalName($basename.text);}
    //basename PARENTHESIS_START predicate PARENTHESIS_END |
    //basename '[' offsets ']' '(' predicate ')'
    ;

basename:
    STRING |
    BASENAME |
    PATH_ANY |
    PATH_ANY_RECURSIVE
;

offsets returns [ JsonOffset result ] @init{$result = new JsonOffset();}:
    INTEGER {$result.setExact($INTEGER.text);} |
    INTEGER {$result.setMin($INTEGER.text);} RANGE_SEP INTEGER {$result.setMax($INTEGER.text);}
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
