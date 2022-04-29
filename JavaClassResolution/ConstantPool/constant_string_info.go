package ConstantPool

import (
	"JavaClassResolution/Reader"
)

type ConstantStringInfo struct {
	index int16
}

func NewConstantStringInfo() *ConstantStringInfo {
	return &ConstantStringInfo{}
}

func (this *ConstantStringInfo) ReadInfo(reader *Reader.ClassReader) {
	this.index = int16(reader.ReadUInt16())
}

func (this *ConstantStringInfo) String() string {
	//return strconv.FormatUint(uint64(this.index), 10)
	return ""
}
