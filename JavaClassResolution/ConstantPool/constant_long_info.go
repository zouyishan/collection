package ConstantPool

import (
	"JavaClassResolution/Reader"
)

type ConstantLongInfo struct {
	val int64
}

func NewConstantLongInfo() *ConstantLongInfo {
	return &ConstantLongInfo{}
}

func (this *ConstantLongInfo) ReadInfo(reader *Reader.ClassReader) {
	this.val = int64(reader.ReadUInt64())
}

func (this *ConstantLongInfo) String() string {
	//return "#" + strconv.FormatUint(uint64(this.val), 10)
	return ""
}
