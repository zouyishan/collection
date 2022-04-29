package ConstantPool

import "JavaClassResolution/Reader"

type ConstantMethodTypeInfo struct {
	descriptorIndex uint16
}

func NewConstantMethodTypeInfo() *ConstantMethodTypeInfo {
	return &ConstantMethodTypeInfo{}
}

func (this *ConstantMethodTypeInfo) ReadInfo(reader *Reader.ClassReader) {
	this.descriptorIndex = reader.ReadUInt16()
}

func (this *ConstantMethodTypeInfo) String() string {
	return ""
}
