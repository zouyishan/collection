package Attribute

import (
	"JavaClassResolution/Reader"
)

type Deprecated struct {
	attributeNameIndex uint16
	attributeLength    uint32
}

func NewDeprecated(nameIndex uint16) *Deprecated {
	return &Deprecated{attributeNameIndex: nameIndex}
}

func (this *Deprecated) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
}
