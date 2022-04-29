package Attribute

import (
	"JavaClassResolution/Reader"
)

type LocalVariableTypeTable struct {
	attributeNameIndex           uint16
	attributeLength              uint32
	localVariableTypeTableLength uint16
	localVariableTypeTable       []LocalVariableTypeTableInfo
}

type LocalVariableTypeTableInfo struct {
	startPc        uint16
	Length         uint16
	NameIndex      uint16
	SignatureIndex uint16
	Index          uint16
}

func NewLocalVariableTypeTable(nameIndex uint16) *LocalVariableTypeTable {
	return &LocalVariableTypeTable{attributeNameIndex: nameIndex}
}

func (this *LocalVariableTypeTable) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
	this.localVariableTypeTableLength = reader.ReadUInt16()
	this.localVariableTypeTable = this.readLocalVariableTypeTable(reader, this.localVariableTypeTableLength)
}

func (this *LocalVariableTypeTable) readLocalVariableTypeTable(reader *Reader.ClassReader, length uint16) []LocalVariableTypeTableInfo {
	var index uint16
	table := make([]LocalVariableTypeTableInfo, length)
	for index = 0; index < length; index++ {
		info := LocalVariableTypeTableInfo{}
		info.startPc = reader.ReadUInt16()
		info.Length = reader.ReadUInt16()
		info.NameIndex = reader.ReadUInt16()
		info.SignatureIndex = reader.ReadUInt16()
		info.Index = reader.ReadUInt16()
		table = append(table, info)
	}
	return table
}
