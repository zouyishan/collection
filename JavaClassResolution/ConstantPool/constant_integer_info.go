package ConstantPool

import (
	"JavaClassResolution/Reader"
)

type ConstantIntegerInfo struct {
	val int32
}

func (this *ConstantIntegerInfo) ReadInfo(reader *Reader.ClassReader) {
	this.val = int32(reader.ReadUInt32())
}

func (this *ConstantIntegerInfo) String() string {
	//return "#" + strconv.FormatUint(uint64(this.val), 10)
	return ""
}

func NewConstantIntegerInfo() *ConstantIntegerInfo {
	return &ConstantIntegerInfo{}
}
