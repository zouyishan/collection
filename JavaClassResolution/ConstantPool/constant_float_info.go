package ConstantPool

import (
	"JavaClassResolution/Reader"
	"math"
)

type ConstantFloatInfo struct {
	val float32
}

func (this *ConstantFloatInfo) ReadInfo(reader *Reader.ClassReader) {
	this.val = math.Float32frombits(reader.ReadUInt32())
}

func (this *ConstantFloatInfo) String() string {
	//return "#" + strconv.FormatFloat(float64(this.val), 'E', -1, 32)
	return ""
}

func NewConstantFloatInfo() *ConstantFloatInfo {
	return &ConstantFloatInfo{}
}
