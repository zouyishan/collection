package Attribute

import (
	"JavaClassResolution/Reader"
)

type Signature struct {
	attributeNameIndex uint16
	attributeLength    uint32
	signatureIndex     uint16
}

func NewSignature(nameIndex uint16) *Signature {
	return &Signature{attributeNameIndex: nameIndex}
}

func (this *Signature) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
	this.signatureIndex = reader.ReadUInt16()
}
