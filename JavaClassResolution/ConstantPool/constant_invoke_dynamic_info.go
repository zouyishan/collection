package ConstantPool

import "JavaClassResolution/Reader"

type ConstantInvokeDynamicInfo struct {
	bootstrapMethodAttrIndex uint16
	nameAndTypeIndex         uint16
}

func NewConstantInvokeDynamicInfo() *ConstantInvokeDynamicInfo {
	return &ConstantInvokeDynamicInfo{}
}

func (self *ConstantInvokeDynamicInfo) ReadInfo(reader *Reader.ClassReader) {
	self.bootstrapMethodAttrIndex = reader.ReadUInt16()
	self.nameAndTypeIndex = reader.ReadUInt16()
}

func (self *ConstantInvokeDynamicInfo) String() string {
	return ""
}
