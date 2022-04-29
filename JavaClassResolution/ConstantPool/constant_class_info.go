package ConstantPool

import (
	"JavaClassResolution/Reader"
	"strconv"
)

type ConstantClassInfo struct {
	index int16
}

func NewConstantClassInfo() *ConstantClassInfo {
	return &ConstantClassInfo{}
}

func (this *ConstantClassInfo) ReadInfo(reader *Reader.ClassReader) {
	this.index = int16(reader.ReadUInt16())
}

func (this *ConstantClassInfo) String() string {
	return strconv.Itoa(int(this.index))
}
