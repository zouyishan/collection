package Attribute

import (
	"JavaClassResolution/ConstantPool"
	"JavaClassResolution/Reader"
)

type Code struct {
	pool                 []ConstantPool.ConstantInfo
	attributeNameIndex   uint16
	attributeLength      uint32
	maxStack             uint16
	maxLocals            uint16
	codeLength           uint32
	code                 []byte
	exceptionTableLength uint16
	exceptionTable       []ExceptionInfo
	attributesCount      uint16
	attributes           []AttributeInfo
}

func NewCode(pool []ConstantPool.ConstantInfo, nameIndex uint16) *Code {
	return &Code{pool: pool, attributeNameIndex: nameIndex}
}

func (this *Code) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
	this.maxStack = reader.ReadUInt16()
	this.maxLocals = reader.ReadUInt16()
	this.codeLength = reader.ReadUInt32()
	this.code = reader.ReadBytes(this.codeLength)
	this.exceptionTableLength = reader.ReadUInt16()
	this.exceptionTable = readExceptionTable(reader, this.exceptionTableLength)
	this.attributesCount = reader.ReadUInt16()
	this.attributes = Parse(reader, this.pool, this.attributesCount)
}

type ExceptionInfo struct {
	startPc   uint16
	endPc     uint16
	handlerPc uint16
	catchType uint16
}

func readExceptionTable(reader *Reader.ClassReader, length uint16) []ExceptionInfo {
	infoList := make([]ExceptionInfo, 0)
	for index := 0; index < int(length); index++ {
		info := ExceptionInfo{}
		info.startPc = reader.ReadUInt16()
		info.endPc = reader.ReadUInt16()
		info.handlerPc = reader.ReadUInt16()
		info.catchType = reader.ReadUInt16()
		infoList = append(infoList, info)
	}
	return infoList
}
