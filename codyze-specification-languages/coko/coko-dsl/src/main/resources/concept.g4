grammar concept;
file: (concept | ruleSet | rule | enum)* EOF;

name: ID | EMPTY_ID;
type: ID;

concept: 'concept' name conceptBody;
conceptBody: '{' conceptItem* '}';
conceptItem: variable | operation | operationPointer;
variable: 'var' name (':' type)?;
operationPointer: 'op' name '=' name ('|' name)*;
operation: 'op' name parameterClause;
parameterClause: '(' parameterList? ')';
parameterList: parameter (',' parameter)*;
parameter: name | name '...';

enum: 'enum' name enumBody;
enumBody: '{' name (',' name)* '}';

ruleSet: 'ruleset' name ruleSetBody;
ruleSetBody: '{' rule+ '}';

rule: 'rule' name ruleBody;
ruleBody: '{' ruleItem* '}';
ruleItem: ruleVariable | when;

ruleVariable: 'var' name ':' type;
when: 'when' condition '{' whenBody '}';
whenBody: assert*;

assert: assertCall | assertEnsure;

assertCall: 'call' opReference assertCallLocation?;
assertCallLocation: eogDirection 'in' eogScope;
assertEnsure: 'ensure' booleanExpression;

eogScope: 'function scope'; // currently, we only support function scope
eogDirection: 'afterwards' | 'before' | 'somewhere';

condition: booleanExpression;
expression:
  name '.' name parameterClause #callExpression |
  expression '.' name #memberExpression |
  name #referenceExpression |
  literal #literalExpression;
booleanExpression:
  lhs op rhs #comparison |
  lhs IN array #rangeExpression |
  opReference #opExpression |
  booleanExpression AND booleanExpression #andExpression;
array: '[' arrayElement (',' arrayElement)* ']';
arrayElement: literal | name;
lhs: expression;
op: OPERATOR;
rhs: expression;
literal: LITERAL;

opReference: name '::' (name | WILDCARD) parameterClause;

AND: 'and';
OR: 'or';
IN: 'in';
WILDCARD: '*';
LITERAL: [0-9]+;
EMPTY_ID: '_'; // empty identifier
ID : [a-zA-Z]+;             // identifier
WS : [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines
OPERATOR: '==' | '<=' | '>=' | '!=';
LINE_COMMENT: '//' ~[\r\n]* -> skip; // skip comments