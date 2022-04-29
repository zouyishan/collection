package Attribute

import (
	"JavaClassResolution/Reader"
)

type LineNumberTable struct {
	attributeNameIndex    uint16
	attributeLength       uint32
	lineNumberTableLength uint16
	lineNumberTable       []LineNumberInfo
}

type LineNumberInfo struct {
	startPc    uint16
	lineNumber uint16
}

func NewLineNumberTable(nameIndex uint16) *LineNumberTable {
	return &LineNumberTable{attributeNameIndex: nameIndex}
}

func (this *LineNumberTable) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
	this.lineNumberTableLength = reader.ReadUInt16()
	this.lineNumberTable = make([]LineNumberInfo, 0)
	for i := 0; uint16(i) < this.lineNumberTableLength; i++ {
		lineNumberTable := LineNumberInfo{}
		lineNumberTable.startPc = reader.ReadUInt16()
		lineNumberTable.lineNumber = reader.ReadUInt16()
		this.lineNumberTable = append(this.lineNumberTable, lineNumberTable)
	}
}
