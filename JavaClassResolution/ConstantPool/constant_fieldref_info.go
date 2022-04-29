package ConstantPool

import (
	"JavaClassResolution/Reader"
)

type ConstantFieldRefInfo struct {
	indexClassInfo   uint16
	indexNameAndType uint16
}

func NewConstantFieldRefInfo() *ConstantFieldRefInfo {
	return &ConstantFieldRefInfo{}
}

func (this *ConstantFieldRefInfo) ReadInfo(reader *Reader.ClassReader) {
	this.indexClassInfo = reader.ReadUInt16()
	this.indexNameAndType = reader.ReadUInt16()
}

func (this *ConstantFieldRefInfo) String() string {
	//return "#" + strconv.FormatUint(uint64(this.indexClassInfo), 10) + "#." + strconv.FormatUint(uint64(this.indexNameAndType), 10)
	return ""
}
