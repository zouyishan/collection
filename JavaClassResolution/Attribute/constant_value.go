package Attribute

import (
	"JavaClassResolution/Reader"
)

type ConstantValue struct {
	attributeNameIndex uint16
	attributeLength    uint32
	constantValueIndex uint16
}

func NewConstantValue(nameIndex uint16) *ConstantValue {
	return &ConstantValue{attributeNameIndex: nameIndex}
}

func (this *ConstantValue) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
	this.constantValueIndex = reader.ReadUInt16()
}
