package ConstantPool

import (
	"JavaClassResolution/Reader"
)

type ConstantNameAndTypeInfo struct {
	indexClassInfo   uint16
	indexNameAndType uint16
}

func NewConstantNameAndTypeInfo() *ConstantNameAndTypeInfo {
	return &ConstantNameAndTypeInfo{}
}

func (this *ConstantNameAndTypeInfo) ReadInfo(reader *Reader.ClassReader) {
	this.indexClassInfo = reader.ReadUInt16()
	this.indexNameAndType = reader.ReadUInt16()
}

func (this *ConstantNameAndTypeInfo) String() string {
	//return "#" + strconv.FormatUint(uint64(this.indexClassInfo), 10) + "#." + strconv.FormatUint(uint64(this.indexNameAndType), 10)
	return ""
}
