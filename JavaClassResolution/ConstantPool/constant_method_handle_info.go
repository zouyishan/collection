package ConstantPool

import "JavaClassResolution/Reader"

type ConstantMethodHandleInfo struct {
	referenceKind  uint8
	referenceIndex uint16
}

func NewConstantMethodHandleInfo() *ConstantMethodHandleInfo {
	return &ConstantMethodHandleInfo{}
}

func (this *ConstantMethodHandleInfo) ReadInfo(reader *Reader.ClassReader) {
	this.referenceKind = reader.ReadUInt8()
	this.referenceIndex = reader.ReadUInt16()
}

func (this *ConstantMethodHandleInfo) String() string {
	return ""
}
