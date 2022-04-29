package Attribute

import (
	"JavaClassResolution/Reader"
)

type Synthetic struct {
	attributeNameIndex uint16
	attributeLength    uint32
}

func NewSynthetic(nameIndex uint16) *Synthetic {
	return &Synthetic{attributeNameIndex: nameIndex}
}

func (this *Synthetic) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
}
