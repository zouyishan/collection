package ConstantPool

import (
	"JavaClassResolution/Reader"
)

type ConstantInterfaceMethodRefInfo struct {
	indexClassInfo   uint16
	indexNameAndType uint16
}

func NewConstantInterfaceMethodRefInfo() *ConstantInterfaceMethodRefInfo {
	return &ConstantInterfaceMethodRefInfo{}
}

func (this *ConstantInterfaceMethodRefInfo) ReadInfo(reader *Reader.ClassReader) {
	this.indexClassInfo = reader.ReadUInt16()
	this.indexNameAndType = reader.ReadUInt16()
}

func (this *ConstantInterfaceMethodRefInfo) String() string {
	//return "#" + strconv.FormatUint(uint64(this.indexClassInfo), 10) + ".#" + strconv.FormatUint(uint64(this.indexNameAndType), 10)
	return ""
}
