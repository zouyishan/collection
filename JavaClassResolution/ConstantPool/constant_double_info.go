package ConstantPool

import (
	"JavaClassResolution/Reader"
	"math"
)

type ConstantDoubleInfo struct {
	val float64
}

func NewConstantDoubleInfo() *ConstantDoubleInfo {
	return &ConstantDoubleInfo{}
}

func (this *ConstantDoubleInfo) ReadInfo(reader *Reader.ClassReader) {
	this.val = math.Float64frombits(reader.ReadUInt64())
}

func (this *ConstantDoubleInfo) String() string {
	//return strconv.FormatFloat(this.val, 'E', -1, 64)
	return ""
}
