"GString1 ${ -> heavy(123)}  GString2 ${-> def a=123; return a;}"
"GString3 ${-> def a=123; heavy(a)}"
"GString4 ${heavy(a)} GString5 ${a} ${-> 123} GString6 ${123}"
"GString7 ${} ${->}"
