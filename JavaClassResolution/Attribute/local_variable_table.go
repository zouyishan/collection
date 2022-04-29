package Attribute

import (
	"JavaClassResolution/Reader"
)

type LocalVariableTable struct {
	attributeNameIndex       uint16
	attributeLength          uint32
	localVariableTableLength uint16
	localVariableTable       []LocalVariableInfo
}

type LocalVariableInfo struct {
	startPc         uint16
	length          uint16
	nameIndex       uint16
	descriptorIndex uint16
	index           uint16
}

func NewLocalVariableTable(nameIndex uint16) *LocalVariableTable {
	return &LocalVariableTable{attributeNameIndex: nameIndex}
}

func (this *LocalVariableTable) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
	this.localVariableTableLength = reader.ReadUInt16()
	this.localVariableTable = this.readLocalVariableTable(reader, this.localVariableTableLength)
}

func (this *LocalVariableTable) readLocalVariableTable(reader *Reader.ClassReader, length uint16) []LocalVariableInfo {
	table := make([]LocalVariableInfo, 0)
	for index := 0; index < int(length); index++ {
		info := LocalVariableInfo{}
		info.startPc = reader.ReadUInt16()
		info.length = reader.ReadUInt16()
		info.nameIndex = reader.ReadUInt16()
		info.descriptorIndex = reader.ReadUInt16()
		info.index = reader.ReadUInt16()
		table = append(table, info)
	}
	return table
}
