testing-library
=================

Json Expr Syntax
```
/* escaper */
#@ eol line expr
#%
arbitrary mvel code (mono line or multi-line)
%#
```

Json Comments
```
# [eol commment]
// [eol comment]
/* [multiline 
comment]
*/
xxx /* embedded comment */ yyy
```


Json Predicates
@NotNull
@NotExisting
@Equal(value)
@Contains(value)
@Match(value)
@Count(value)
@Type(numeric|boolean|date)
@Assert(xpath,assertions)
@Eval(method name)

Json XPath Syntax :

```
.. : (parent node)
field1.field2 : access to subfield field2. If field1 is an array, it corresponds to list of all field2 within the array 
field1(0).field2 : field2 of first element. raise an empty list if field1 is not an array
field1.*.field2 : access to subfield field2 with 1 intermediate field name (unknown name)
field1.**.field2 : access to subfield field2 what ever intermediate fields existing
field1(xpath : assertions)
field1(=predicates)
```

Json extended Syntax :
    xpath : string value
       String values can be :
            'simple value'
            "simple value"
            "simple #%xxxxx%# with interpolation"
            <<EOF
                a multi-line text
            EOF (position defines the caracters to skip)

    xpath : numeric
    xpath : true
    xpath : false
    xpath : { subchecks }
    xpath : [ { }, { } ] //exact array match
    xpath : [0]=> { }, [2..5,$-1,$]=> { } //partial array match ($ = last element)
    xpath : null
    xpath : 
    xpath : @xxx(...) @xxx(...) //meta node data
    xpath : @xxx(...) @xxx(...) + sub node datas/values,...

