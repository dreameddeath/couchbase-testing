/** Taken from "The Definitive ANTLR 4 Reference" by Terence Parr */
// Derived from http://json.org
parser grammar JSON_DATASET;

@header{
  import java.util.Map;
  import java.util.HashMap;
  import java.util.Collections;
  import java.util.List;
  import java.util.ArrayList;
  import com.dreameddeath.testing.dataset.model.*;
}

options {   tokenVocab = JSON_DATASET_LEXER; }

dataset returns [ Dataset result ]
       @init{ $result=new Dataset(); }
   :
    (dataset_elt {$result.addElement($dataset_elt.result); } ) *
    ;


monoline_rule returns [ DatasetMvel result ] @init{ $result=new DatasetMvel();} :
    MVEL_MONOLINE_START
        (MVEL_MONOLINE_CONTENT {$result.addContent($MVEL_MONOLINE_CONTENT.text);})*
    MVEL_MONOLINE_END
    ;

multiline_rule returns [ DatasetMvel result ] @init{ $result=new DatasetMvel();} :
    MVEL_MULTILINE
    (
        MVEL_MULTILINE_COMMENT_COMPLEX {$result.addContent($MVEL_MULTILINE_COMMENT_COMPLEX.text);}|
        MVEL_MULTILINE_COMMENT_SIMPLE {$result.addContent($MVEL_MULTILINE_COMMENT_SIMPLE.text);}|
        MVEL_MULTILINE_DQ_STRING {$result.addContent($MVEL_MULTILINE_DQ_STRING.text);}|
        MVEL_MULTILINE_SQ_STRING {$result.addContent($MVEL_MULTILINE_SQ_STRING.text);}|
        MVEL_MULTILINE_CONTENT {$result.addContent($MVEL_MULTILINE_CONTENT.text);}
     ) *
    MVEL_MULTILINE_END
    ;



dataset_elt returns [ DatasetElement result ]
    @init{ $result=new DatasetElement(); }
:
 (meta_data {$result.addMeta($meta_data.result);})* object { $result.setObject($object.result);}
| (meta_data {$result.addMeta($meta_data.result);})* array {$result.setArray($array.result);}
| monoline_rule {$result.setMvel($monoline_rule.result);}
| multiline_rule {$result.setMvel($multiline_rule.result);}
| directive {$result.setDirective($directive.result);}
;

object returns [ DatasetObject result ]
       @init{ $result = new DatasetObject(); }
: OBJECT_START pair {$result.addNode($pair.result);} (COMMA pair {$result.addNode($pair.result);} )* OBJECT_END //{ $result = $attributes;}
| OBJECT_START OBJECT_END // empty object
;

pair returns [ DatasetObjectNode result ]
        @init{ $result=new DatasetObjectNode();}:
    dataset_path { $result.setXPath($dataset_path.result);} FIELD_VAL_SEP value {$result.setValue($value.result);}
    ;

directive returns [ DatasetDirective result ]
    @init{ $result=new DatasetDirective();}
    :
 DIRECTIVE BASENAME {$result.setName($BASENAME.text);} PARENTHESIS_START (value {$result.addParam($value.result);} (COMMA value {$result.addParam($value.result);}) *) ? PARENTHESIS_END
;
dataset_path
    returns [ DatasetXPath result ]
    @init{$result = new DatasetXPath();}
 :
    (meta_data {$result.addMeta($meta_data.result);})* path_part {$result.addPart($path_part.result);} ( DOT? path_part {$result.addPart($path_part.result);} )*
    ;


meta_data returns [ DatasetMeta result ] @init{$result = new DatasetMeta();}:
    META_CHAR basename {$result.setName($basename.text);} |
    META_CHAR basename {$result.setName($basename.text);} PARENTHESIS_START PARENTHESIS_END |
    META_CHAR basename {$result.setName($basename.text);} PARENTHESIS_START value {$result.addParam($value.result);}(COMMA value {$result.addParam($value.result);})* PARENTHESIS_END
    ;



path_part returns [ DatasetXPathPart result ] @init{$result=new DatasetXPathPart();}:
    basename {$result.setLocalName($basename.text);} |
    ARRAY_START range {$result.setRange($range.result);} ARRAY_END
    //basename PARENTHESIS_START predicate PARENTHESIS_END |
    //basename '[' offsets ']' '(' predicate ')'
    ;

basename:
    STRING |
    BASENAME |
    PATH_ANY |
    PATH_ANY_RECURSIVE
;

range returns [ DatasetRange result ] @init{$result = new DatasetRange();}:
    INTEGER {$result.setExact($INTEGER.text);} |
    INTEGER {$result.setMin($INTEGER.text);} RANGE_SEP INTEGER {$result.setMax($INTEGER.text);} |
    ;

array
    returns [ List<DatasetValue> result ]
    @init{$result= new ArrayList();}
: ARRAY_START value {$result.add($value.result);} (COMMA value {$result.add($value.result);} )* ARRAY_END//{ $result = $elements;}
| ARRAY_START ARRAY_END // empty array
;

value returns [ DatasetValue result ]
@init{$result=new DatasetValue();}
: (meta_data {$result.addMeta($meta_data.result);})* STRING { $result.setStrValue($STRING.text);}
| (meta_data {$result.addMeta($meta_data.result);})* DECIMAL { $result.setDecimalValue($DECIMAL.text); }
| (meta_data {$result.addMeta($meta_data.result);})* INTEGER { $result.setLongValue($INTEGER.text);}
| (meta_data {$result.addMeta($meta_data.result);})* EXP_INT { $result.setLongValue($EXP_INT.text);}
| (meta_data {$result.addMeta($meta_data.result);})* DATETIME { $result.setDateTime($DATETIME.text);}
| (meta_data {$result.addMeta($meta_data.result);})* object { $result.setObjectValue($object.result);}// recursion
| (meta_data {$result.addMeta($meta_data.result);})* array { $result.setArrayValue($array.result);}// recursion
| (meta_data {$result.addMeta($meta_data.result);})* TRUE { $result.setBool(true);}// keywords
| (meta_data {$result.addMeta($meta_data.result);})* FALSE { $result.setBool(false);}
| (meta_data {$result.addMeta($meta_data.result);})* NULL { $result.setNull();}
| (meta_data {$result.addMeta($meta_data.result);})* multiline_rule {$result.setMvel($multiline_rule.result);}
| (meta_data {$result.addMeta($meta_data.result);})+ {$result.setEmpty();}
;
