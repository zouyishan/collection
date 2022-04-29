package ConstantPool

import (
	"JavaClassResolution/Reader"
)

type ConstantMethodRefInfo struct {
	indexClassInfo   uint16
	indexNameAndType uint16
}

func NewConstantMethodRefInfo() *ConstantMethodRefInfo {
	return &ConstantMethodRefInfo{}
}

func (this *ConstantMethodRefInfo) ReadInfo(reader *Reader.ClassReader) {
	this.indexClassInfo = reader.ReadUInt16()
	this.indexNameAndType = reader.ReadUInt16()
}

func (this *ConstantMethodRefInfo) String() string {
	//return "#" + strconv.FormatUint(uint64(this.indexClassInfo), 10) + ".#" + strconv.FormatUint(uint64(this.indexNameAndType), 10)
	return ""
}
