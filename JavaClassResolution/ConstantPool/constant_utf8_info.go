package ConstantPool

import "JavaClassResolution/Reader"

type ConstantUtf8Info struct {
	val string
}

func (this *ConstantUtf8Info) ReadInfo(reader *Reader.ClassReader) {
	length := reader.ReadUInt16()
	bytes := reader.ReadBytes(uint32(length))
	this.val = string(bytes)
}

func (this *ConstantUtf8Info) String() string {
	return this.val
}

func NewConstantUtf8Info() *ConstantUtf8Info {
	return &ConstantUtf8Info{}
}
